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
import java.io.InputStream;

/**
 * Represents an image which can be added to an RTF document.
 */
public class RtfPicture {
  /**
   * The image type of the picture. Usually AUTOMATIC does the job.
   */
  public enum PictureType {
    /**
     * Type of the image will be figures out automatially.
     */
    AUTOMATIC,

    /**
     * Image type is JPG.
     */
    JPG {
      @Override public String toString() {return RtfControlWords.JPEG_PICTURE;}
    },

    /**
     * Image type is PNG.
     */
    PNG {
      @Override public String toString() {return RtfControlWords.PNG_PICTURE;}
    },

    /**
     * Image type is an enhanced metafile (EMF).
     */
    EMF {
      @Override public String toString() {return RtfControlWords.EMF_PICTURE;}
    },
  }

  /**
   * Functional interface to lazily open the image's byte source, so the stream is
   * only read when the enclosing document is actually written.
   */
  @FunctionalInterface
  interface StreamSource {
    InputStream open() throws IOException;
  }

  private final StreamSource source;
  private final StringBuilder hexPicData = new StringBuilder( 4096 );
  private boolean loaded = false;

  private int widthInTwips = -1, heightInTwips = -1;
  private int scaleX = -1, scaleY = -1;

  /*
   * <pict>         : = '{' \pict
   *                    (<brdr>? &
   *                     <shading>? &
   *                     <picttype> &
   *                     <pictsize> &
   *                     <metafileinfo>? )
   *                    <data> '}'
   * <picttype>     := \emfblip |
   *                   \pngblip |
   *                   \jpegblip |
   *                   \macpict |
   *                   \pmmetafile |
   *                   \wmetafile |
   *                   \dibitmap <bitmapinfo> |
   *                   \wbitmap <bitmapinfo>
   * <bitmapinfo>   := \wbmbitspixel & \wbmplanes & \wbmwidthbytes
   * <pictsize>     := (\picw? & \pich?)
   *                   \picscalex? &
   *                   \picscaley? &
   *                   \picscaled? &
   *                   \piccropt? &
   *                   \piccropb? &
   *                   \piccropr? &
   *                   \piccropl?
   * <metafileinfo> := \picbmp & \picbpp
   * <data>         := ( \bin #BDATA) | #SDATA
   */

  /**
   * Stores the image source for later, lazy reading. The stream is only opened and
   * hex-encoded once the enclosing RTF document is actually written via {@code out()}.
   *
   * @param source Source of the image. Must not be {@code null}.
   */
  RtfPicture( StreamSource source ) {
    this.source = source;
  }

  /**
   * Reads the image from its source and hex-encodes it, but only once (on first call).
   * Called from {@link #type(PictureType)}'s render body, i.e. only when the document
   * is written.
   */
  private void ensureLoaded() throws IOException {
    if ( loaded )
      return;

    try ( InputStream in = new java.io.BufferedInputStream( source.open() ) ) {
      byte[] buf = new byte[ 4096 ];
      int pos = 0;  // hex chars written on current line (0..39)
      int n;
      while ( (n = in.read( buf )) != -1 ) {
        for ( int i = 0; i < n; i++ ) {
          int b = buf[ i ] & 0xFF;
          if ( b < 16 ) hexPicData.append( '0' );
          hexPicData.append( Integer.toHexString( b ) );
          if ( ++pos == 40 ) {
            pos = 0;
            hexPicData.append( '\n' );
          }
        }
      }
    }

    loaded = true;
  }

  /**
   * Width of the image.
   *
   * @param width Width of the image.
   * @param unit  Measurement.
   * @return {@code this}-object.
   */
  public RtfPicture width( double width, RtfUnit unit ) {
    widthInTwips = unit.toTwips( width );
    return this;
  }

  /**
   * Height of the image.
   *
   * @param height Height of the image.
   * @param unit   Measurement.
   * @return {@code this}-object.
   */
  public RtfPicture height( double height, RtfUnit unit ) {
    heightInTwips = unit.toTwips( height );
    return this;
  }

  /**
   * Size of the picture.
   *
   * @param width  Width of the image.
   * @param height Height of the image.
   * @param unit   Measurement.
   * @return {@code this}-object.
   */
  public RtfPicture size( double width, double height, RtfUnit unit ) {
    widthInTwips = unit.toTwips( width );
    heightInTwips = unit.toTwips( height );
    return this;
  }

  /**
   * Horizontal scaling. Default is 100.
   *
   * @param scale Scale.
   * @return {@code this}-object.
   */
  public RtfPicture scaleX( int scale ) {
    scaleX = scale;
    return this;
  }

  /**
   * Vertical scaling. Default is 100.
   *
   * @param scale Scale.
   * @return {@code this}-object.
   */
  public RtfPicture scaleY( int scale ) {
    scaleY = scale;
    return this;
  }

  /**
   * Scale the image.
   *
   * @param scaleX Scale for X.
   * @param scaleY Scale for Y.
   * @return {@code this}-object.
   */
  public RtfPicture scale( int scaleX, int scaleY ) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    return this;
  }

  /**
   * Sets the type and finish setting a picture.
   *
   * @param pictureType Type of this picture. Usually {@link PictureType#AUTOMATIC} is a good choise.
   * @return RtfText.
   */
  public RtfText type( PictureType pictureType ) {
    if ( pictureType == null )
      throw new IllegalArgumentException( "PictureType must not be null" );
    return new RtfText( out -> {
      try { ensureLoaded(); }
      catch ( IOException e ) { throw new RtfException( e ); }

      out.open( RtfControlWords.PICTURE_DESTINATION );

      if ( pictureType == PictureType.AUTOMATIC ) {
        if ( hexPicData.length() < 10 * 2 )
          throw new RtfException( "Image is too small to detect picture type automatically. "
                                + "Pass an explicit PictureType for images smaller than 10 bytes." );
        // Find out the picture type by poking in the first bytes
        String hexChar1 = hexPicData.substring( 1 * 2, 1 * 2 + 2 );
        String hexChar2 = hexPicData.substring( 2 * 2, 2 * 2 + 2 );
        String hexChar3 = hexPicData.substring( 3 * 2, 3 * 2 + 2 );
        String hexChar6 = hexPicData.substring( 6 * 2, 6 * 2 + 2 );
        String hexChar7 = hexPicData.substring( 7 * 2, 7 * 2 + 2 );
        String hexChar8 = hexPicData.substring( 8 * 2, 8 * 2 + 2 );
        String hexChar9 = hexPicData.substring( 9 * 2, 9 * 2 + 2 );

        char char1 = (char) Integer.parseInt( hexChar1, 16 );
        char char2 = (char) Integer.parseInt( hexChar2, 16 );
        char char3 = (char) Integer.parseInt( hexChar3, 16 );
        char char6 = (char) Integer.parseInt( hexChar6, 16 );
        char char7 = (char) Integer.parseInt( hexChar7, 16 );
        char char8 = (char) Integer.parseInt( hexChar8, 16 );
        char char9 = (char) Integer.parseInt( hexChar9, 16 );

        if ( char6 == 'J' && char7 == 'F' && char8 == 'I' && char9 == 'F' )
          out.cw( PictureType.JPG.toString() );
        else if ( char1 == 'P' && char2 == 'N' && char3 == 'G' )
          out.cw( PictureType.PNG.toString() );
        else
          throw new RtfException(
              "Unsupported image type. Pass an explicit PictureType for formats "
              + "other than JPG and PNG." );
      }
      else
        out.cw( pictureType.toString() );

      if ( widthInTwips != -1 )
        out.cw( RtfControlWords.PICTURE_WIDTH_GOAL ).append( widthInTwips );
      if ( heightInTwips != -1 )
        out.cw( RtfControlWords.PICTURE_HEIGHT_GOAL ).append( heightInTwips );

      if ( scaleX != -1 )
        out.cw( RtfControlWords.PICTURE_SCALE_X ).append( scaleX );
      if ( scaleY != -1 )
        out.cw( RtfControlWords.PICTURE_SCALE_Y ).append( scaleY );

      out.nl();
      out.append( hexPicData );
      out.close();
    } );
  }
}
