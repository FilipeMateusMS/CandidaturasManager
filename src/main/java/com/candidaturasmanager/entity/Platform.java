package com.candidaturasmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table( name = "platforms" )
public class Platform
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long cdPlatform;

    @Column( nullable = false, length = 100 )
    private String nmPlatform;

    @Column( length = 500 )
    private String dsPlatform;

    @Column( nullable = false, length = 1000 )
    private String dsUrl;

    private LocalDateTime dtUltimoAcesso;

    @Column( nullable = false )
    private Boolean stAtivo = true;

    public Long getCdPlatform()
    {
        return cdPlatform;
    }

    public String getNmPlatform()
    {
        return nmPlatform;
    }

    public void setNmPlatform( String nmPlatform )
    {
        this.nmPlatform = nmPlatform;
    }

    public String getDsPlatform()
    {
        return dsPlatform;
    }

    public void setDsPlatform( String dsPlatform )
    {
        this.dsPlatform = dsPlatform;
    }

    public String getDsUrl()
    {
        return dsUrl;
    }

    public void setDsUrl( String dsUrl )
    {
        this.dsUrl = dsUrl;
    }

    public LocalDateTime getDtUltimoAcesso()
    {
        return dtUltimoAcesso;
    }

    public void setDtUltimoAcesso( LocalDateTime dtUltimoAcesso )
    {
        this.dtUltimoAcesso = dtUltimoAcesso;
    }

    public Boolean getStAtivo()
    {
        return stAtivo;
    }

    public void setStAtivo( Boolean stAtivo )
    {
        this.stAtivo = stAtivo;
    }
}
