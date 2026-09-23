package com.candidaturasmanager.controller;

import com.candidaturasmanager.dto.PlatformImportResult;
import com.candidaturasmanager.dto.PlatformRequest;
import com.candidaturasmanager.entity.Platform;
import com.candidaturasmanager.exception.PlatformValidationException;
import com.candidaturasmanager.service.PlatformService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PlatformController
{
    private final PlatformService service;

    public PlatformController( PlatformService service )
    {
        this.service = service;
    }

    @GetMapping( "/" )
    public String index( Model model )
    {
        model.addAttribute( "plataformas", service.listarAtivas() );
        model.addAttribute( "inativas", service.listarInativas() );

        return "index";
    }

    @GetMapping( "/novo" )
    public String novo( Model model )
    {
        model.addAttribute( "platformRequest", new PlatformRequest() );
        model.addAttribute( "modoEdicao", false );

        return "form";
    }

    @GetMapping( "/importar" )
    public String importar()
    {
        return "importar";
    }

    @PostMapping( "/importar" )
    public String importar(
        @RequestParam( "arquivo" ) MultipartFile arquivo,
        RedirectAttributes redirectAttributes )
    {
        PlatformImportResult result = service.importarCsv( arquivo );

        if ( result.possuiErros() )
        {
            redirectAttributes.addFlashAttribute( "errosImportacao", result.getErros() );

            return "redirect:/importar";
        }

        redirectAttributes.addFlashAttribute(
            "sucesso",
            result.getQuantidadeImportada() + " plataforma(s) importada(s) com sucesso."
        );

        return "redirect:/";
    }

    @GetMapping( "/visualizar/{cdPlatform}" )
    public String visualizar( @PathVariable Long cdPlatform, Model model )
    {
        model.addAttribute( "plataforma", service.buscarPorId( cdPlatform ) );

        return "visualizar";
    }

    @PostMapping( "/salvar" )
    public String salvar(
        @Valid @ModelAttribute( "platformRequest" ) PlatformRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model )
    {
        if ( bindingResult.hasErrors() )
        {
            model.addAttribute( "modoEdicao", false );

            return "form";
        }

        try
        {
            service.salvar( request );
        }
        catch ( PlatformValidationException exception )
        {
            bindingResult.rejectValue(
                exception.getField(),
                null,
                exception.getMessage()
            );

            model.addAttribute( "modoEdicao", false );

            return "form";
        }

        redirectAttributes.addFlashAttribute( "sucesso", "Plataforma cadastrada com sucesso." );

        return "redirect:/";
    }

    @GetMapping( "/editar/{cdPlatform}" )
    public String editar( @PathVariable Long cdPlatform, Model model )
    {
        Platform platform = service.buscarPorId( cdPlatform );
        PlatformRequest request = new PlatformRequest();

        request.setNmPlatform( platform.getNmPlatform() );
        request.setDsPlatform( platform.getDsPlatform() );
        request.setDsUrl( platform.getDsUrl() );

        model.addAttribute( "platformRequest", request );
        model.addAttribute( "cdPlatform", cdPlatform );
        model.addAttribute( "modoEdicao", true );

        return "form";
    }

    @PostMapping( "/editar/{cdPlatform}" )
    public String editar(
        @PathVariable Long cdPlatform,
        @Valid @ModelAttribute( "platformRequest" ) PlatformRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model )
    {
        if ( bindingResult.hasErrors() )
        {
            model.addAttribute( "modoEdicao", true );
            model.addAttribute( "cdPlatform", cdPlatform );

            return "form";
        }

        try
        {
            service.editar( cdPlatform, request );
        }
        catch ( PlatformValidationException exception )
        {
            bindingResult.rejectValue(
                exception.getField(),
                null,
                exception.getMessage()
            );

            model.addAttribute( "modoEdicao", true );
            model.addAttribute( "cdPlatform", cdPlatform );

            return "form";
        }

        redirectAttributes.addFlashAttribute( "sucesso", "Plataforma atualizada com sucesso." );

        return "redirect:/";
    }

    @PostMapping( "/toggle/{cdPlatform}" )
    public String alternarStatus(
        @PathVariable Long cdPlatform,
        RedirectAttributes redirectAttributes )
    {
        service.alternarStatus( cdPlatform );
        redirectAttributes.addFlashAttribute( "sucesso", "Status da plataforma atualizado com sucesso." );

        return "redirect:/";
    }

    @PostMapping( "/acessar/{cdPlatform}" )
    public String acessar( @PathVariable Long cdPlatform )
    {
        Platform platform = service.acessar( cdPlatform );

        return "redirect:" + platform.getDsUrl();
    }
}
