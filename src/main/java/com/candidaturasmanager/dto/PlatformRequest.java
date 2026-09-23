package com.candidaturasmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlatformRequest
{
    @NotBlank( message = "Informe o nome da plataforma." )
    @Size( max = 100, message = "O nome deve possuir no máximo 100 caracteres." )
    private String nmPlatform;

    @Size( max = 500, message = "A descrição deve possuir no máximo 500 caracteres." )
    private String dsPlatform;

    @NotBlank( message = "Informe a URL." )
    @Size( max = 1000, message = "A URL deve possuir no máximo 1000 caracteres." )
    private String dsUrl;

    public String getNmPlatform() { return nmPlatform; }
    public void setNmPlatform( String nmPlatform ) { this.nmPlatform = nmPlatform; }
    public String getDsPlatform() { return dsPlatform; }
    public void setDsPlatform( String dsPlatform ) { this.dsPlatform = dsPlatform; }
    public String getDsUrl() { return dsUrl; }
    public void setDsUrl( String dsUrl ) { this.dsUrl = dsUrl; }
}
