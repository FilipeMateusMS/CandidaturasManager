package com.candidaturasmanager.service;

import com.candidaturasmanager.dto.PlatformRequest;
import com.candidaturasmanager.entity.Platform;
import com.candidaturasmanager.exception.PlatformNotFoundException;
import com.candidaturasmanager.exception.PlatformValidationException;
import com.candidaturasmanager.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
