/*
 * Copyright (c) 2010-2026 Christian Ullenboom
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

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
public class RtfTemplate {
  /**
   * Source of the template, read lazily on first {@link #out()}.
   */
  private @Nullable InputStream inputStream;

  /**
   * Holds the template file, populated lazily on first {@link #out()}.
   */
  private final StringBuilder template = new StringBuilder( 8192 );

  /**
   * {@code true} once {@link #inputStream} has been read into {@link #template}.
   */
  private boolean loaded = false;

  /**
   * Map with all variables and substitutions.
   */
  private final Map<String, Object> map = new HashMap<>();

  /**
   * Regex pattern for %%VARIABLE%%.
   */
  private final static Pattern VARIABLE_PATTERN = Pattern.compile( "%%(\\S+)%%",
                                                                   Pattern.DOTALL | Pattern.MULTILINE );

  /**
   * Stores the {@link InputStream} for later, lazy reading. The bytes are only read
   * (in Windows-1252 encoding) and the stream only closed once {@link #out()} is
   * first called, not when this constructor runs.
   *
   * @param inputStream Source of the template. Must not be {@code null}.
   */
  RtfTemplate( InputStream inputStream ) {
    if ( inputStream == null )
      throw new IllegalArgumentException( "InputStream can't be null" );

    this.inputStream = inputStream;
  }

  /**
   * Reads {@link #inputStream} into {@link #template}, but only once.
   */
  private void ensureLoaded() {
    if ( loaded )
      return;

    InputStream stream = inputStream;
    if ( stream == null )
      return;

    InputStream in = stream instanceof BufferedInputStream
                    ? stream : new BufferedInputStream( stream );

    try ( Reader reader = new InputStreamReader( in, Rtf.CHARSET1252 ) ) {
      for ( int c; (c = reader.read()) != -1; )
        template.append( (char) c );
    }
    catch ( IOException e ) {
      throw new RtfException( e );
    }

    inputStream = null;
    loaded = true;
  }

  /**
   * Adds key/values pairs for a variable substitution. Keep the keys in pure ASCII.
   *
   * @param map Map with key/value pairs.
   * @return {@code this} object.
   */
  public RtfTemplate inject( Map<String, Object> map ) {
    this.map.putAll( map );
    return this;
  }

  /**
   * Adds a key/value pair for substitution. Keep the key in pure ASCII.
   *
   * @param key   Key. Must not be {@code null}.
   * @param value Value. May be {@code null}, in which case the literal text {@code "null"}
   *              is substituted; otherwise resolved like a single element passed to
   *              {@link RtfText#text(Object...)} (a plain {@code String} is escaped as text,
   *              an already-built {@link RtfText} is inserted as-is with its formatting, etc.).
   * @return {@code this} object.
   */
  public RtfTemplate inject( String key, @Nullable Object value ) {
    if ( key == null )
      throw new IllegalArgumentException( "Key can't be null" );

    map.put( key, value == null ? "null" : value );
    return this;
  }

  /**
   * Performs the variable transformation and returns the
   * transformed RTF document.
   *
   * @return RTF document after variable substitution.
   */
  public String out() {
    ensureLoaded();

    if ( map.isEmpty() )
      return template.toString();

    StringBuffer result = new StringBuffer( template.length() );
    Matcher matcher = VARIABLE_PATTERN.matcher( template );

    while ( matcher.find() ) {
      Object value = map.get( matcher.group( 1 ) );

      if ( value == null )
        continue;

      StringBuilder sb = new StringBuilder( 128 );
      RtfOutput out = new RtfOutput( sb );
      RtfText.text( value ).rtf( out );
      matcher.appendReplacement( result, Matcher.quoteReplacement( sb.toString() ) );
    }

    matcher.appendTail( result );

    return result.toString();
  }

  /**
   * Performs the variable transformation and writes the RTF document and send
   * the output to an {@link Appendable}. This method closes the {@link Appendable}
   * after writing if its of type {@link Closeable}.
   *
   * @param out Destination of this RTF output.
   */
  public void out( OutputStream out ) {
    try ( OutputStream os = out ) {
      os.write( out().getBytes( Rtf.CHARSET1252 ) );
    }
    catch ( IOException e ) {
      throw new RtfException( e );
    }
  }
}
