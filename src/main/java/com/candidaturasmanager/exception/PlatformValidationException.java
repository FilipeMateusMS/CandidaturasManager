package com.candidaturasmanager.exception;

public class PlatformValidationException extends RuntimeException
{
    private final String field;

    public PlatformValidationException( String field, String message )
    {
        super( message );
        this.field = field;
    }

    public String getField()
    {
        return field;
    }
}
