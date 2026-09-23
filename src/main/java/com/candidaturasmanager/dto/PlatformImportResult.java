package com.candidaturasmanager.dto;

import java.util.ArrayList;
import java.util.List;

public class PlatformImportResult
{
    private int quantidadeImportada;
    private final List<String> erros = new ArrayList<>();

    public int getQuantidadeImportada()
    {
        return quantidadeImportada;
    }

    public void setQuantidadeImportada( int quantidadeImportada )
    {
        this.quantidadeImportada = quantidadeImportada;
    }

    public List<String> getErros()
    {
        return erros;
    }

    public boolean possuiErros()
    {
        return !erros.isEmpty();
    }
}
