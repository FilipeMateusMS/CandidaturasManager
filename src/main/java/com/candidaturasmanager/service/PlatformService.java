package com.candidaturasmanager.service;

import com.candidaturasmanager.dto.PlatformRequest;
import com.candidaturasmanager.entity.Platform;
import com.candidaturasmanager.exception.PlatformNotFoundException;
import com.candidaturasmanager.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
        return repository.save( platform );
    }

    private String normalizar( String valor )
    {
        if ( valor == null || valor.isBlank() )
        {
            return null;
        }

        return valor.trim();
    }
}
