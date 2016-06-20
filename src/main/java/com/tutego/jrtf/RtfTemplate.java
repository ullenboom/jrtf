/*
 * Copyright (c) 2010-2016 Christian Ullenboom 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jRTF' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tutego.jrtf;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * This class is able to make variable substitutions in an
 * existing RTF file.
 * <pre>
 * Rtf.template( new FileInputStream("template.rtf) )
 *    .inject( "ADDRESSLINE1", "tutego" )
 *    .inject( "ADDRESSLINE2", bold("Sonsbeck") )
 *    .out( FileOutputStream("out.rtf") );
 * </pre>
 * Just use ASCII-keys.
 * 
 * <p>
 * Variables must be framed in {@code %%} like
 * <pre>
 * %%ADDRESSLINE1%%
 * </pre>
 */
public class RtfTemplate
{
  /** Holds the template file. */
  private StringBuilder template = new StringBuilder( 8192 );

  /** Map with all variables and substitutions. */
  private Map<String, Object> map = new HashMap<String, Object>();

  /** Regex pattern for %%VARIABLE%%. */
  private final static Pattern VARIABLE_PATTERN = Pattern.compile( "%%(\\S+)%%",
                                                                   Pattern.DOTALL | Pattern.MULTILINE);

  /**
   * Reads the template from that {@link InputStream}. The method treats the
   * bytes in Windows-1252 encoding. After reading the stream will be closed.
   * @param inputStream
   */
  RtfTemplate( InputStream inputStream )
  {
    Reader reader = null;
    try
    {
      if ( ! ( inputStream instanceof BufferedInputStream ) )
        inputStream = new BufferedInputStream( inputStream );

      reader = new InputStreamReader( inputStream, Rtf.CHARSET1252 );

      for ( int c; (c = reader.read() )!= -1; )
        template.append( (char ) c );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
    finally
    {
      if ( reader != null )
        try { reader.close(); } catch ( IOException e ) { throw new RtfException( e ); }
    }
  }

  /**
   * Adds key/values pairs for a variable substitution. Keep the keys in pure ASCII.
   * @param map Map with key/value pairs.
   * @return {@code this} object.
   */
  public RtfTemplate inject( Map<String, Object> map )
  {
    this.map.putAll( map );  
    return this;
  }

  /**
   * Adds a key/value pair for substitution. Keep the key in pure ASCII.
   * The value will be converted to String by {@link String#valueOf(Object)}.
   * @param key   Key.
   * @param value Value.
   * @return {@code this} object.
   */
  public RtfTemplate inject( String key, Object value )
  {
    map.put( key, String.valueOf( value ) );
    return this;
  }

  /**
   * Performs the variable transformation and returns the
   * transformed RTF document.
   * @return RTF document after variable substitution.
   */
  public String out()
  {
    if ( map.isEmpty() )
      return template.toString();

    StringBuffer result = new StringBuffer( template.length() );
    Matcher matcher = VARIABLE_PATTERN.matcher( template );
  
    while ( matcher.find() )
    {
      Object value = map.get( matcher.group( 1 ) );
  
      if ( value == null )
        continue;

      StringBuilder sb = new StringBuilder( 128 );
      try
      {
        RtfText.text( value ).rtf( sb );
      }
      catch ( IOException e )
      {
        throw new RtfException( e );
      }
      matcher.appendReplacement( result, Matcher.quoteReplacement( sb.toString() ) );
    }

    matcher.appendTail( result );
  
    return result.toString();
  }

  /**
   * Performs the variable transformation and writes the RTF document and send
   * the output to an {@link Appendable}. This method closes the {@link Appendable} 
   * after writing if its of type {@link Closeable}.
   * @param out Destination of this RTF output.
   */
  public void out( OutputStream out )
  {
    try
    {
      String out2 = out();
      out.write( out2.getBytes(Rtf.CHARSET1252 ) );
    }
    catch ( IOException e )
    {
      throw new RtfException( e );
    }
    finally
    {
      try { out.close(); } catch ( IOException e ) { throw new RtfException(e); }
    }
  }
}
