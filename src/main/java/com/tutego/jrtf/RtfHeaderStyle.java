/*
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

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

/**
 * Represents a style sheet definition for the RTF header. Instances are built once and
 * their RTF is only ever produced when the enclosing document is written (see {@link #toString()}),
 * consistent with the rest of the library never building an in-memory document.
 * <p>
 * The six built-in styles ({@link #NORMAL}, {@link #HEADER_1} .. {@link #HEADER_5}) keep their
 * original, fixed style ids. Additional, user-defined styles can be created with
 * {@link #builder(String)}; their id is assigned once they
 * are registered with {@link Rtf#headerStyles(RtfHeaderStyle...)}.
 */
public final class RtfHeaderStyle {
  /**
   * Default style.
   */
  public static final RtfHeaderStyle NORMAL = new RtfHeaderStyle( 0, out -> out.append( "Normal;" ) );

  /**
   * Header 1 Style.
   */
  public static final RtfHeaderStyle HEADER_1 = new RtfHeaderStyle( 1, out -> out.append( "Heading 1;" ) );

  /**
   * Header 2 Style.
   */
  public static final RtfHeaderStyle HEADER_2 = new RtfHeaderStyle( 2, out -> out.append( "Heading 2;" ) );

  /**
   * Header 3 Style.
   */
  public static final RtfHeaderStyle HEADER_3 = new RtfHeaderStyle( 3, out -> out.append( "Heading 3;" ) );

  /**
   * Header 4 Style.
   */
  public static final RtfHeaderStyle HEADER_4 = new RtfHeaderStyle( 4, out -> out.append( "Heading 4;" ) );

  /**
   * Header 5 Style.
   */
  public static final RtfHeaderStyle HEADER_5 = new RtfHeaderStyle( 5, out -> out.append( "Heading 5;" ) );

  private static final RtfHeaderStyle[] BUILTINS =
      { NORMAL, HEADER_1, HEADER_2, HEADER_3, HEADER_4, HEADER_5 };

  /**
   * Returns the built-in styles, in the same declaration order as the historic
   * {@code RtfHeaderStyle.values()} of the time this class was still an enum.
   *
   * @return The six built-in styles.
   */
  public static RtfHeaderStyle[] builtins() {
    return BUILTINS.clone();
  }

  /**
   * Style sheet id. Unassigned ({@code -1}) for custom styles until they are
   * registered with {@link Rtf#headerStyles(RtfHeaderStyle...)}.
   */
  private int id;

  /**
   * Render body for this style, evaluated only when the document is written.
   */
  private final Consumer<RtfOutput> renderer;

  private RtfHeaderStyle( int id, Consumer<RtfOutput> renderer ) {
    this.id = id;
    this.renderer = renderer;
  }

  /**
   * Starts building a custom style with the fluent {@link Builder}.
   *
   * @param name Style name, shown in word processor UIs (e.g. "My Style").
   * @return New {@link Builder}.
   */
  public static Builder builder( String name ) {
    return new Builder( name );
  }

  /**
   * Fluent builder for custom styles, deferring all RTF generation to the point the
   * enclosing document is written, like every other RTF-producing part of this library.
   */
  public static final class Builder {
    private final String name;
    private @Nullable RtfHeaderStyle basedOn;
    private final StringBuilder charFmt = new StringBuilder();

    private Builder( String name ) {
      if ( name == null || name.isEmpty() )
        throw new IllegalArgumentException( "Style name is missing" );
      this.name = name;
    }

    /**
     * The new style inherits from an existing style.
     *
     * @param style Style this one is based on. Must already be registered (have an id).
     * @return {@code this}-object.
     */
    public Builder basedOn( RtfHeaderStyle style ) {
      this.basedOn = style;
      return this;
    }

    /**
     * Uses the given font, as defined in the header font table.
     *
     * @param fontnum Font number according to the header.
     * @return {@code this}-object.
     */
    public Builder font( int fontnum ) {
      charFmt.append( '\\' ).append( RtfControlWords.FONT ).append( fontnum );
      return this;
    }

    /**
     * Sets the font size in half-points.
     *
     * @param fontSize Font size.
     * @return {@code this}-object.
     */
    public Builder fontSize( int fontSize ) {
      charFmt.append( '\\' ).append( RtfControlWords.FONT_SIZE ).append( fontSize );
      return this;
    }

    /**
     * Bold text.
     *
     * @return {@code this}-object.
     */
    public Builder bold() {
      charFmt.append( '\\' ).append( RtfControlWords.BOLD );
      return this;
    }

    /**
     * Italic text.
     *
     * @return {@code this}-object.
     */
    public Builder italic() {
      charFmt.append( '\\' ).append( RtfControlWords.ITALIC );
      return this;
    }

    /**
     * Centers the paragraph.
     *
     * @return {@code this}-object.
     */
    public Builder alignCentered() {
      charFmt.append( '\\' ).append( RtfControlWords.ALIGN_CENTERED );
      return this;
    }

    /**
     * Builds the style. Not registered yet; register it with
     * {@link Rtf#headerStyles(RtfHeaderStyle...)} to assign its id.
     *
     * @return New unregistered {@code RtfHeaderStyle}.
     */
    public RtfHeaderStyle build() {
      String escapedName = Rtf.asRtf( name );
      RtfHeaderStyle basedOnStyle = basedOn;
      String charFmtSnapshot = charFmt.toString();

      return new RtfHeaderStyle( -1, out -> {
        if ( basedOnStyle != null )
          out.cw( RtfControlWords.BASED_ON_STYLE ).append( basedOnStyle.getId() );
        out.append( charFmtSnapshot );
        out.sp().append( escapedName ).semi();
      } );
    }
  }

  /**
   * Assigns an id to this style if it doesn't have one yet. Called only by
   * {@link Rtf#headerStyles(RtfHeaderStyle...)}, never mutates already-registered
   * (including built-in) styles.
   *
   * @param newId Id to assign.
   */
  void assignIdIfUnassigned( int newId ) {
    if ( id == -1 )
      id = newId;
  }

  /**
   * Returns document style sheet id.
   *
   * @return id
   */
  public int getId() {
    return id;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder( 128 );
    RtfOutput out = new RtfOutput( sb );
    out.open().cw( RtfControlWords.STYLE ).append( id ).sp();
    renderer.accept( out );
    out.close();
    return sb.toString();
  }
}
