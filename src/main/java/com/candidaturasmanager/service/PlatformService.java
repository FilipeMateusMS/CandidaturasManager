package com.candidaturasmanager.service;

import com.candidaturasmanager.dto.PlatformImportResult;
import com.candidaturasmanager.dto.PlatformRequest;
import com.candidaturasmanager.entity.Platform;
import com.candidaturasmanager.exception.PlatformNotFoundException;
import com.candidaturasmanager.exception.PlatformValidationException;
import com.candidaturasmanager.repository.PlatformRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PlatformService
{
    private final PlatformRepository repository;

    public PlatformService( PlatformRepository repository )
    {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Platform> listarAtivas()
    {
        return repository.findByStAtivoTrueOrderByDtUltimoAcessoAsc();
    }

    @Transactional(readOnly = true)
    public List<Platform> listarInativas()
    {
        return repository.findByStAtivoFalseOrderByNmPlatformAsc();
    }

    @Transactional(readOnly = true)
    public Platform buscarPorId( Long cdPlatform )
    {
        return repository.findById( cdPlatform )
            .orElseThrow( () -> new PlatformNotFoundException( "Plataforma não encontrada." ) );
    }

    @Transactional
    public void salvar( PlatformRequest request )
    {
        validar( request, null );

        Platform platform = new Platform();
        platform.setNmPlatform( request.getNmPlatform().trim() );
        platform.setDsPlatform( normalizar( request.getDsPlatform() ) );
        platform.setDsUrl( request.getDsUrl().trim() );
        platform.setStAtivo( true );

        repository.save( platform );
    }

    @Transactional
    public void editar( Long cdPlatform, PlatformRequest request )
    {
        Platform platform = buscarPorId( cdPlatform );

        validar( request, cdPlatform );

        platform.setNmPlatform( request.getNmPlatform().trim() );
        platform.setDsPlatform( normalizar( request.getDsPlatform() ) );
        platform.setDsUrl( request.getDsUrl().trim() );

        repository.save( platform );
    }

    @Transactional
    public void alternarStatus( Long cdPlatform )
    {
        Platform platform = buscarPorId( cdPlatform );
        platform.setStAtivo( !platform.getStAtivo() );
        repository.save( platform );
    }

    @Transactional
    public Platform acessar( Long cdPlatform )
    {
        Platform platform = buscarPorId( cdPlatform );

        if ( !platform.getStAtivo() )
        {
            throw new PlatformNotFoundException( "A plataforma está desativada." );
        }

        platform.setDtUltimoAcesso( LocalDateTime.now() );

        return repository.saveAndFlush( platform );
    }

    @Transactional
    public PlatformImportResult importarCsv( MultipartFile arquivo )
    {
        PlatformImportResult result = new PlatformImportResult();

        if ( arquivo == null || arquivo.isEmpty() )
        {
            result.getErros().add( "Selecione um arquivo CSV." );
            return result;
        }

        try
        {
            String conteudo = new String( arquivo.getBytes(), StandardCharsets.UTF_8 )
                .replace( "﻿", "" );

            char separador = detectarSeparador( conteudo );

            CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setDelimiter( separador )
                .setHeader()
                .setSkipHeaderRecord( true )
                .setIgnoreEmptyLines( true )
                .setTrim( true )
                .get();

            try ( CSVParser parser = CSVParser.parse( new StringReader( conteudo ), formato ) )
            {
                Set<String> cabecalhos = new HashSet<>();

                parser.getHeaderMap().keySet().forEach(
                    cabecalho -> cabecalhos.add( normalizarCabecalho( cabecalho ) )
                );

                if ( !cabecalhos.contains( "nome" )
                    || !cabecalhos.contains( "descricao" )
                    || !cabecalhos.contains( "url" ) )
                {
                    result.getErros().add(
                        "O CSV deve possuir as colunas: nome, descricao e url."
                    );

                    return result;
                }

                List<Platform> plataformas = new ArrayList<>();
                Set<String> nomes = new HashSet<>();
                Set<String> urls = new HashSet<>();

                for ( CSVRecord registro : parser )
                {
                    int linha = (int) registro.getRecordNumber() + 1;

                    String nmPlatform = obterValor( registro, "nome" );
                    String dsPlatform = obterValor( registro, "descricao" );
                    String dsUrl = obterValor( registro, "url" );

                    if ( nmPlatform.isBlank() )
                    {
                        result.getErros().add( "Linha " + linha + ": informe o nome." );
                        continue;
                    }

                    if ( dsUrl.isBlank() )
                    {
                        result.getErros().add( "Linha " + linha + ": informe a URL." );
                        continue;
                    }

                    if ( nmPlatform.length() > 100 )
                    {
                        result.getErros().add(
                            "Linha " + linha + ": o nome deve possuir no máximo 100 caracteres."
                        );
                        continue;
                    }

                    if ( dsPlatform.length() > 500 )
                    {
                        result.getErros().add(
                            "Linha " + linha + ": a descrição deve possuir no máximo 500 caracteres."
                        );
                        continue;
                    }

                    if ( dsUrl.length() > 1000 )
                    {
                        result.getErros().add(
                            "Linha " + linha + ": a URL deve possuir no máximo 1000 caracteres."
                        );
                        continue;
                    }

                    String nmNormalizado = nmPlatform.trim().toLowerCase( Locale.ROOT );
                    String urlNormalizada = normalizarUrl( dsUrl );

                    boolean nomeDuplicadoArquivo = !nomes.add( nmNormalizado );
                    boolean urlDuplicadaArquivo = !urls.add( urlNormalizada );

                    if ( nomeDuplicadoArquivo )
                    {
                        result.getErros().add(
                            "Linha " + linha + ": já existe outra plataforma com este nome no arquivo."
                        );
                        continue;
                    }

                    if ( urlDuplicadaArquivo )
                    {
                        result.getErros().add(
                            "Linha " + linha + ": já existe outra plataforma com esta URL no arquivo."
                        );
                        continue;
                    }

                    plataformas.add( criarPlatform( nmPlatform, dsPlatform, dsUrl ) );
                }

                validarDuplicidadesNoBanco( plataformas, result );

                if ( result.possuiErros() )
                {
                    return result;
                }

                repository.saveAll( plataformas );
                result.setQuantidadeImportada( plataformas.size() );
            }
        }
        catch ( IOException exception )
        {
            result.getErros().add( "Não foi possível ler o arquivo CSV." );
        }
        catch ( IllegalArgumentException exception )
        {
            result.getErros().add( "O formato do arquivo CSV é inválido." );
        }

        return result;
    }

    public void validar( PlatformRequest request, Long cdPlatform )
    {
        String nmPlatform = request.getNmPlatform().trim();
        String dsUrl = request.getDsUrl().trim();
        String dsUrlNormalizada = normalizarUrl( dsUrl );

        boolean nomeDuplicado = repository.findAll().stream()
            .filter( platform -> cdPlatform == null || !platform.getCdPlatform().equals( cdPlatform ) )
            .anyMatch( platform ->
                platform.getNmPlatform().trim().equalsIgnoreCase( nmPlatform ) );

        if ( nomeDuplicado )
        {
            throw new PlatformValidationException(
                "nmPlatform",
                "Já existe uma plataforma com este nome."
            );
        }

        boolean urlDuplicada = repository.findAll().stream()
            .filter( platform -> cdPlatform == null || !platform.getCdPlatform().equals( cdPlatform ) )
            .anyMatch( platform ->
                normalizarUrl( platform.getDsUrl() ).equals( dsUrlNormalizada ) );

        if ( urlDuplicada )
        {
            throw new PlatformValidationException(
                "dsUrl",
                "Já existe uma plataforma com esta URL."
            );
        }
    }

    private Platform criarPlatform( String nmPlatform, String dsPlatform, String dsUrl )
    {
        Platform platform = new Platform();
        platform.setNmPlatform( nmPlatform.trim() );
        platform.setDsPlatform( normalizar( dsPlatform ) );
        platform.setDsUrl( dsUrl.trim() );
        platform.setStAtivo( true );

        return platform;
    }

    private void validarDuplicidadesNoBanco(
        List<Platform> plataformas,
        PlatformImportResult result )
    {
        List<Platform> existentes = repository.findAll();

        for ( Platform platform : plataformas )
        {
            boolean nomeDuplicado = existentes.stream()
                .anyMatch( existente ->
                    existente.getNmPlatform().trim().equalsIgnoreCase( platform.getNmPlatform().trim() ) );

            if ( nomeDuplicado )
            {
                result.getErros().add(
                    "A plataforma "" + platform.getNmPlatform() + "" já está cadastrada."
                );
                continue;
            }

            String urlNormalizada = normalizarUrl( platform.getDsUrl() );

            boolean urlDuplicada = existentes.stream()
                .anyMatch( existente ->
                    normalizarUrl( existente.getDsUrl() ).equals( urlNormalizada ) );

            if ( urlDuplicada )
            {
                result.getErros().add(
                    "A URL da plataforma "" + platform.getNmPlatform() + "" já está cadastrada."
                );
            }
        }
    }

    private String obterValor( CSVRecord registro, String nomeColuna )
    {
        for ( String cabecalho : registro.getParser().getHeaderMap().keySet() )
        {
            if ( normalizarCabecalho( cabecalho ).equals( nomeColuna ) )
            {
                return registro.get( cabecalho ).trim();
            }
        }

        return "";
    }

    private String normalizarCabecalho( String cabecalho )
    {
        return cabecalho
            .replace( "﻿", "" )
            .trim()
            .toLowerCase( Locale.ROOT )
            .replace( "ç", "c" )
            .replace( "ã", "a" )
            .replace( "á", "a" )
            .replace( "é", "e" );
    }

    private char detectarSeparador( String conteudo )
    {
        int fimCabecalho = conteudo.indexOf( '\n' );

        if ( fimCabecalho < 0 )
        {
            return ',';
        }

        String cabecalho = conteudo.substring( 0, fimCabecalho );

        return cabecalho.contains( ";" ) && !cabecalho.contains( "," )
            ? ';'
            : ',';
    }

    private String normalizar( String valor )
    {
        if ( valor == null || valor.isBlank() )
        {
            return null;
        }

        return valor.trim();
    }

    private String normalizarUrl( String dsUrl )
    {
        try
        {
            URI uri = new URI( dsUrl.trim() ).normalize();

            String scheme = uri.getScheme() == null
                ? null
                : uri.getScheme().toLowerCase();

            String host = uri.getHost() == null
                ? null
                : uri.getHost().toLowerCase();

            String path = uri.getPath();

            if ( path == null || path.isBlank() )
            {
                path = "/";
            }

            String query = normalizarParametros( uri.getRawQuery() );

            URI uriNormalizada = new URI(
                scheme,
                uri.getUserInfo(),
                host,
                uri.getPort(),
                path,
                query,
                null
            );

            return uriNormalizada.toASCIIString();
        }
        catch ( URISyntaxException exception )
        {
            return dsUrl.trim().toLowerCase();
        }
    }

    private String normalizarParametros( String query )
    {
        if ( query == null || query.isBlank() )
        {
            return null;
        }

        List<String> parametros = new ArrayList<>( List.of( query.split( "&" ) ) );
        parametros.sort( Comparator.naturalOrder() );

        return String.join( "&", parametros );
    }
}
