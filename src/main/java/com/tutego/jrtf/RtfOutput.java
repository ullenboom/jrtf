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

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Internal RTF output target. Wraps an {@link Appendable} (typically a user-supplied
 * {@link java.io.Writer} for streaming) and provides fluent append methods for all
 * common types, plus RTF-specific helpers for control words, grouping, and punctuation.
 * <p>
 * All public methods catch {@link IOException} from the underlying {@link Appendable}
 * and rethrow it as a {@link RtfException}. Callers never see a checked exception.
 */
final class RtfOutput {

  private final Appendable out;

  /**
   * Wraps the given {@link Appendable} so callers can write RTF without
   * checked {@link IOException} — every public method catches and rethrows
   * as {@link RtfException}.
   */
  RtfOutput( Appendable out ) {
    this.out = out;
  }

  // ---- Basic appends (no checked exceptions) ----

  public RtfOutput append( CharSequence cs ) { try { out.append( cs ); } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( char c )           { try { out.append( c );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( int i )            { try { out.append( Integer.toString( i ) ); } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( long l )           { try { out.append( Long.toString( l ) );    } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( float f )          { try { out.append( Float.toString( f ) );   } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( double d )         { try { out.append( Double.toString( d ) );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( boolean b )        { try { out.append( Boolean.toString( b ) ); } catch ( IOException e ) { throw new RtfException( e ); } return this; }
  public RtfOutput append( Object o )         { try { out.append( o.toString() );          } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  // ---- Low-level control-word helpers (auto-prepend backslash) ----

  /**
   * Writes a raw control word with no trailing delimiter: {@code \word}.
   */
  public RtfOutput cw( String word ) {
    try { out.append( '\\' ).append( word ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a control word with a numeric value and no trailing delimiter: {@code \wordN}.
   */
  public RtfOutput cw( String word, int value ) {
    try { out.append( '\\' ).append( word ).append( Integer.toString( value ) ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a flag-style control word with newline delimiter: {@code \word\n}.
   */
  public RtfOutput ctrl( String word ) {
    try { out.append( '\\' ).append( word ).append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a valued control word with newline delimiter: {@code \wordN\n}.
   */
  public RtfOutput ctrl( String word, int value ) {
    try { out.append( '\\' ).append( word ).append( Integer.toString( value ) ).append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a control word with string value and newline delimiter: {@code \word value\n}.
   */
  public RtfOutput ctrl( String word, String value ) {
    try { out.append( '\\' ).append( word ).append( value ).append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  // ---- Higher-level key-value helpers ----

  /**
   * Writes a key-value pair: {@code \wordN\n}. Semantically clearer than
   * {@link #ctrl(String, int)} when the word is a property with an associated value.
   */
  public RtfOutput pair( String word, int value ) {
    try { out.append( '\\' ).append( word ).append( Integer.toString( value ) ).append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a key-value pair with a string value: {@code \word value\n}.
   */
  public RtfOutput pair( String word, String value ) {
    try { out.append( '\\' ).append( word ).append( value ).append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  // ---- Punctuation / delimiters ----

  /** Appends a newline. */
  public RtfOutput nl()    { try { out.append( '\n' ); } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  /** Appends a space. */
  public RtfOutput sp()    { try { out.append( ' ' );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  /** Appends a semicolon. */
  public RtfOutput semi()  { try { out.append( ';' );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  // ---- Hex escape ----

  /**
   * Writes a hex-escaped byte ({@code \'xx}) as used in font table entries
   * and list-level text definitions.
   */
  public RtfOutput hexChar( int byteValue ) {
    try {
      out.append( "\\'" );
      String hex = Integer.toHexString( byteValue & 0xFF );
      if ( hex.length() < 2 ) out.append( '0' );
      out.append( hex );
    } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  // ---- Grouping (stack / push-pop) ----

  /**
   * Opens an RTF group: writes a left brace.
   * Every call must be balanced by a matching {@link #close()}.
   */
  public RtfOutput open()  { try { out.append( '{' );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  /** Closes an RTF group: writes {@code \}}. */
  public RtfOutput close() { try { out.append( '}' );  } catch ( IOException e ) { throw new RtfException( e ); } return this; }

  /**
   * Opens an RTF group with a leading control word, e.g. {@code \b}.
   * Every call must be balanced by a matching {@link #close()}.
   */
  public RtfOutput open( String controlWord ) {
    try { out.append( '{' ).append( '\\' ).append( controlWord ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Closes an RTF group preceded by a semicolon: writes {@code ;\}}.
   */
  public RtfOutput closeSemi() {
    try { out.append( ";}" ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  // ---- Higher-level grouping ----

  /**
   * Writes a group containing a single nested control word: {@code {\word\innerword}}.
   * Typical for paragraph-numbering text definitions and similar one-word groups.
   */
  public RtfOutput nest( String word, String innerWord ) {
    try { out.append( '{' ).append( '\\' ).append( word ).append( '\\' ).append( innerWord ).append( '}' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a group containing a nested control word with a numeric value:
   * {@code {\word\innerwordN}}.
   */
  public RtfOutput nest( String word, String innerWord, int value ) {
    try { out.append( '{' ).append( '\\' ).append( word ).append( '\\' ).append( innerWord ).append( Integer.toString( value ) ).append( '}' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a semicolon-terminated group entry: {@code {\word content;}}.
   * Common pattern for font-table, color-table and style-sheet definitions.
   */
  public RtfOutput entry( String word, CharSequence content ) {
    try { out.append( '{' ).append( '\\' ).append( word ).append( content ).append( ";}" ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Writes a group around the given content: {@code {\word content}}.
   * The group is always closed.
   */
  public RtfOutput tag( String word, CharSequence content ) {
    try { out.append( '{' ).append( '\\' ).append( word ).append( content ).append( '}' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  // ---- Lambda-based grouping ----

  /**
   * Wraps content in an RTF group with a leading control word.
   * The group is always closed, even if the block throws.
   */
  public RtfOutput group( String controlWord, Consumer<RtfOutput> block ) {
    try { out.append( '{' ).append( '\\' ).append( controlWord ); } catch ( IOException e ) { throw new RtfException( e ); }
    block.accept( this );
    try { out.append( '}' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

  /**
   * Wraps content in an RTF group.
   */
  public RtfOutput group( Runnable block ) {
    try { out.append( '{' ); } catch ( IOException e ) { throw new RtfException( e ); }
    block.run();
    try { out.append( '}' ); } catch ( IOException e ) { throw new RtfException( e ); }
    return this;
  }

}
