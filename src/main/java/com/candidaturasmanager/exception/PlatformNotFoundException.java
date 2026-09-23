package com.candidaturasmanager.exception;

public class PlatformNotFoundException extends RuntimeException
{
    public PlatformNotFoundException( String message )
    {
        super( message );
    }
}
