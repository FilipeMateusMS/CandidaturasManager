package com.candidaturasmanager.controller;

import com.candidaturasmanager.exception.PlatformNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler( PlatformNotFoundException.class )
    public String handlePlatformNotFound( PlatformNotFoundException exception, Model model )
    {
        model.addAttribute( "mensagem", exception.getMessage() );
        return "error";
    }

    @ExceptionHandler( Exception.class )
    public String handleException( Exception exception, Model model )
    {
        model.addAttribute( "mensagem", "Ocorreu um erro inesperado ao processar a solicitação." );
        return "error";
    }
}
