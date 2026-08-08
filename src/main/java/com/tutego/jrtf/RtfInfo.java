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

/**
 * Represents meta information about the RTF document like author, title, keywords.
 */
public class RtfInfo {

  private final Consumer<RtfOutput> renderer;

  private RtfInfo( Consumer<RtfOutput> renderer ) {
    this.renderer = renderer;
  }

  void rtf( RtfOutput out ) {
    renderer.accept( out );
  }

  /*
   * <info> := '{' <title>? &
   *           <subject>? &
   *           <author>? &
   *           <operator>? &
   *           <keywords>? &
   *           <comment>? &
   *           \version? &
   *           <doccomm>? &
   *           \vern? &
   *           <creatim>? &
   *           <revtim>? &
   *           <printim>? &
   *           <buptim>? &
   *           \edmins? &
   *           \nofpages? &
   *           \nofwords? & \id? '}'
   *
   * <operator> '{' \ operator #PCDATA '}'
   * <keywords> '{' \ keywords #PCDATA '}'
   * <comment> '{' \ comment #PCDATA '}'
   * <doccomm> '{' \ doccomm #PCDATA '}'
   * <creatim> '{' \ creatim <time> '}'
   * <revtim> '{' \ revtim <time> '}'
   * <printim> '{' \ printim <time> '}'
   * <buptim> '{' \ buptim <time> '}'
   * <time> \yr? \mo? \dy? \hr? \min? \sec?
   */

  // <subject> '{' \subject #PCDATA '}'
  public static RtfInfo subject( String subject ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_SUBJECT ).sp()
                                     .append( Rtf.asRtf( subject ) ).close() );
  }

  // <title> '{' \title #PCDATA '}'
  public static RtfInfo title( String title ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_TITLE ).sp()
                                     .append( Rtf.asRtf( title ) ).close() );
  }

  // <author> '{' \author #PCDATA '}'
  public static RtfInfo author( String author ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_AUTHOR ).sp()
                                     .append( Rtf.asRtf( author ) ).close() );
  }

  // <keywords> '{' \keywords #PCDATA '}'
  public static RtfInfo keywords( String keywords ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_KEYWORDS ).sp()
                                     .append( Rtf.asRtf( keywords ) ).close() );
  }

  // <comment> '{' \comment #PCDATA '}'
  public static RtfInfo comment( String comment ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_COMMENT ).sp()
                                     .append( Rtf.asRtf( comment ) ).close() );
  }

  // <operator> '{' \operator #PCDATA '}'
  public static RtfInfo operator( String operator ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_OPERATOR ).sp()
                                     .append( Rtf.asRtf( operator ) ).close() );
  }

  // <doccomm> '{' \doccomm #PCDATA '}'
  public static RtfInfo doccomm( String comment ) {
    return new RtfInfo( out -> out.open().cw( RtfControlWords.INFO_DOC_COMMENT ).sp()
                                     .append( Rtf.asRtf( comment ) ).close() );
  }

  // ---- Numeric info entries ----

  // \version?
  public static RtfInfo version( int version ) {
    return new RtfInfo( out -> out.tag( RtfControlWords.INFO_VERSION,
                                         Integer.toString( version ) ) );
  }

  // \nofwords?
  public static RtfInfo numberOfWords( int numberOfWords ) {
    return new RtfInfo( out -> out.tag( RtfControlWords.INFO_NUMBER_OF_WORDS,
                                         Integer.toString( numberOfWords ) ) );
  }

  // \nofpages?
  public static RtfInfo numberOfPages( int numberOfPages ) {
    return new RtfInfo( out -> out.tag( RtfControlWords.INFO_NUMBER_OF_PAGES,
                                         Integer.toString( numberOfPages ) ) );
  }

  // ---- Time-based info entries ----

  // <creatim> '{' \creatim <time> '}'
  public static RtfInfo creatim( int year, int month, int dayOfMonth,
                                 int hour, int minute, int second ) {
    return timeInfo( RtfControlWords.INFO_CREATION_TIME, year, month, dayOfMonth,
                     hour, minute, second );
  }

  // <revtim> '{' \revtim <time> '}'
  public static RtfInfo revtim( int year, int month, int dayOfMonth,
                                int hour, int minute, int second ) {
    return timeInfo( RtfControlWords.INFO_REVISION_TIME, year, month, dayOfMonth,
                     hour, minute, second );
  }

  // <printim> '{' \printim <time> '}'
  public static RtfInfo printim( int year, int month, int dayOfMonth,
                                 int hour, int minute, int second ) {
    return timeInfo( RtfControlWords.INFO_PRINT_TIME, year, month, dayOfMonth,
                     hour, minute, second );
  }

  private static RtfInfo timeInfo( String destination, int year, int month, int dayOfMonth,
                                   int hour, int minute, int second ) {
    return new RtfInfo( out -> {
      out.open().cw( destination ).sp()
         .cw( RtfControlWords.INFO_YEAR ).append( year ).sp()
         .cw( RtfControlWords.INFO_MONTH ).append( month ).sp()
         .cw( RtfControlWords.INFO_DAY ).append( dayOfMonth ).sp()
         .cw( RtfControlWords.INFO_HOUR ).append( hour ).sp()
         .cw( RtfControlWords.INFO_MINUTE ).append( minute ).sp()
         .cw( RtfControlWords.INFO_SECOND ).append( second )
         .close();
    } );
  }
}
