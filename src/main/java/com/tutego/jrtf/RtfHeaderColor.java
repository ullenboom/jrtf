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

/**
 * Represents a color definition for the RTF header.
 */
public class RtfHeaderColor extends RtfHeader implements Comparable<RtfHeaderColor> {
  /**
   * Constant for color black.
   */
  public static final RtfHeaderColor BLACK = new RtfHeaderColor( 0, 0, 0 );

  /**
   * Constant for color white.
   */
  public static final RtfHeaderColor WHITE = new RtfHeaderColor( 255, 255, 255 );

  /**
   * Red, Green, Blue.
   */
  private final int r;
  private final int g;
  private final int b;

  /**
   * Index of the color.
   */
  final int colorindex;

  /**
   * Package visible constructor. The user will not instantiate this class.
   *
   * @param r Red.
   * @param g Green.
   * @param b Blue.
   */
  RtfHeaderColor( int r, int g, int b ) {
    this( r, g, b, 0 );
  }

  private RtfHeaderColor( int r, int g, int b, int colorindex ) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.colorindex = colorindex;
  }

  /**
   * Sets a color at a certain index. The index has to be between 1 and 255 otherwise a {@code RtfException}
   * will be thrown. Index 0 is reserved for the AUTO color.
   * Returns a new instance; the original is unchanged.
   *
   * @param colorindex Index of the color.
   * @return New {@link RtfHeader} with the assigned index.
   */
  public RtfHeader at( int colorindex ) {
    if ( colorindex < 1 || colorindex > 255 )
      throw new RtfException(
          "Color index " + colorindex + " ist out of range, has to be between 1 and 255" );

    return new RtfHeaderColor( r, g, b, colorindex );
  }

  public int compareTo( RtfHeaderColor other ) {
    return this.colorindex - other.colorindex;
  }

  // Theme color names from RTF 1.7+

  /**
   * Pre-defined theme colors available in modern word processors.
   * Use via {@code RtfHeaderColor.theme(ThemeColor.MAIN_DARK_1).at(1)}.
   * <p>
   * Theme colors adapt to the document's overall colour scheme
   * (e.g. dark mode vs light mode).
   */
  public enum ThemeColor {
    MAIN_DARK_1( "cmaindarkone" ),
    MAIN_LIGHT_1( "cmainlightone" ),
    MAIN_DARK_2( "cmaindarktwo" ),
    MAIN_LIGHT_2( "cmainlighttwo" ),
    ACCENT_1( "caccentone" ),
    ACCENT_2( "caccenttwo" ),
    ACCENT_3( "caccentthree" ),
    ACCENT_4( "caccentfour" ),
    ACCENT_5( "caccentfive" ),
    ACCENT_6( "caccentsix" ),
    HYPERLINK( "chyperlink" ),
    FOLLOWED_HYPERLINK( "cfollowedhyperlink" ),
    BACKGROUND_1( "cbackgroundone" ),
    BACKGROUND_2( "cbackgroundtwo" ),
    TEXT_COLOUR( "ctextcolour" );

    final String controlWord;

    ThemeColor( String controlWord ) {
      this.controlWord = controlWord;
    }
  }

  /**
   * Creates a color reference to one of the document's theme colours
   * instead of a fixed RGB value. The theme colour adapts to the
   * document's overall colour scheme.
   *
   * @param color Theme colour.
   * @return New {@code RtfHeaderColor} referencing the theme.
   */
  public static RtfHeaderColor theme( ThemeColor color ) {
    return new ThemeRtfHeaderColor( color );
  }

  /**
   * Appends the color definition of one color in RTF format.
   *
   * @param out Output buffer.
   */
  void writeColordef( RtfOutput out ) {
    /*
     * <colordef> := \red ? & \green ? & \blue ? ';'
     */
    out.cw( RtfControlWords.RED ).append( r )
       .cw( RtfControlWords.GREEN ).append( g )
       .cw( RtfControlWords.BLUE ).append( b )
       .semi();
  }

  // Theme color subclass

  private static final class ThemeRtfHeaderColor extends RtfHeaderColor {
    private final ThemeColor themeColor;

    ThemeRtfHeaderColor( ThemeColor themeColor ) {
      super( 0, 0, 0 );
      this.themeColor = themeColor;
    }

    @Override void writeColordef( RtfOutput out ) {
      out.cw( themeColor.controlWord ).semi();
    }
  }
}