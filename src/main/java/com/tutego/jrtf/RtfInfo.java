/*
 * Copyright (c) 2010-2014 Christian Ullenboom
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
 * Represents meta information about the RTF document like author, title, keywords.
 */
public class RtfInfo {
  /**
   * RTF result of this info.
   */
  final String rtf;

  /**
   * Initializes this object.
   */
  private RtfInfo( String rtf ) {
    this.rtf = rtf;
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

  /**
   * Sets the subject of this document.
   *
   * @param subject Subject.
   * @return New RtfInfo object.
   */
  public static RtfInfo subject( String subject ) {
    // <subject> '{' \subject #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_SUBJECT + " " + Rtf.asRtf( subject ) + "}" );
  }

  /**
   * Sets the title of this document.
   *
   * @param title Title.
   * @return New RtfInfo object.
   */
  public static RtfInfo title( String title ) {
    // <title> '{' \title #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_TITLE + " " + Rtf.asRtf( title ) + "}" );
  }

  /**
   * Sets the author of this document.
   *
   * @param author Author.
   * @return New RtfInfo object.
   */
  public static RtfInfo author( String author ) {
    // <author> '{' \author #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_AUTHOR + " " + Rtf.asRtf( author ) + "}" );
  }

  /**
   * Sets the create time of this document.
   *
   * @param year       Year.
   * @param month      Month.
   * @param dayOfMonth Day of month.
   * @param hour       Hour.
   * @param minute     Minute.
   * @param second     Second.
   * @return New RtfInfo object.
   */
  public static RtfInfo creatim( int year, int month, int dayOfMonth, int hour, int minute,
                                 int second ) {
    // <creatim> '{' \ creatim <time> '}'
    return new RtfInfo( timeGroup( RtfControlWords.INFO_CREATION_TIME, year, month, dayOfMonth, hour, minute, second ) );
  }

  /**
   * Sets the revision (last-save) time of this document.
   *
   * @param year       Year.
   * @param month      Month.
   * @param dayOfMonth Day of month.
   * @param hour       Hour.
   * @param minute     Minute.
   * @param second     Second.
   * @return New RtfInfo object.
   */
  public static RtfInfo revtim( int year, int month, int dayOfMonth, int hour, int minute,
                                int second ) {
    // <revtim> '{' \ revtim <time> '}'
    return new RtfInfo( timeGroup( RtfControlWords.INFO_REVISION_TIME, year, month, dayOfMonth, hour, minute, second ) );
  }

  /**
   * Sets the last-print time of this document.
   *
   * @param year       Year.
   * @param month      Month.
   * @param dayOfMonth Day of month.
   * @param hour       Hour.
   * @param minute     Minute.
   * @param second     Second.
   * @return New RtfInfo object.
   */
  public static RtfInfo printim( int year, int month, int dayOfMonth, int hour, int minute,
                                 int second ) {
    // <printim> '{' \ printim <time> '}'
    return new RtfInfo( timeGroup( RtfControlWords.INFO_PRINT_TIME, year, month, dayOfMonth, hour, minute, second ) );
  }

  /**
   * Builds a {@code {\destination \yrN \moN \dyN \hrN \minN \secN}} time group.
   */
  private static String timeGroup( String destination, int year, int month, int dayOfMonth,
                                   int hour, int minute, int second ) {
    return String.format( "{%s " + RtfControlWords.INFO_YEAR + "%d " + RtfControlWords.INFO_MONTH + "%d "
                          + RtfControlWords.INFO_DAY + "%d " + RtfControlWords.INFO_HOUR + "%d "
                          + RtfControlWords.INFO_MINUTE + "%d " + RtfControlWords.INFO_SECOND + "%d}",
                          destination, year, month, dayOfMonth, hour, minute, second );
  }

  /**
   * Sets the keywords of this document.
   *
   * @param keywords Keywords.
   * @return New RtfInfo object.
   */
  public static RtfInfo keywords( String keywords ) {
    // <keywords> '{' \ keywords #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_KEYWORDS + " " + Rtf.asRtf( keywords ) + "}" );
  }

  /**
   * Sets a comment (abstract) for this document, stored in the info group,
   * not visible in the document body.
   *
   * @param comment Comment.
   * @return New RtfInfo object.
   */
  public static RtfInfo comment( String comment ) {
    // <comment> '{' \ comment #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_COMMENT + " " + Rtf.asRtf( comment ) + "}" );
  }

  /**
   * Sets the name of the operator who last modified this document.
   *
   * @param operator Operator name.
   * @return New RtfInfo object.
   */
  public static RtfInfo operator( String operator ) {
    // <operator> '{' \ operator #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_OPERATOR + " " + Rtf.asRtf( operator ) + "}" );
  }

  /**
   * Sets a comment for this document, associated with the document (as opposed to
   * {@link #comment(String)}, which is a plain info-group comment).
   *
   * @param comment Comment.
   * @return New RtfInfo object.
   */
  public static RtfInfo doccomm( String comment ) {
    // <doccomm> '{' \ doccomm #PCDATA '}'
    return new RtfInfo( "{" + RtfControlWords.INFO_DOC_COMMENT + " " + Rtf.asRtf( comment ) + "}" );
  }

  /**
   * Sets the internal version number of this document.
   *
   * @param version Version number.
   * @return New RtfInfo object.
   */
  public static RtfInfo version( int version ) {
    return new RtfInfo( "{" + RtfControlWords.INFO_VERSION + version + "}" );
  }

  /**
   * Sets the number of words in this document (for use by a document-management utility).
   *
   * @param numberOfWords Number of words.
   * @return New RtfInfo object.
   */
  public static RtfInfo numberOfWords( int numberOfWords ) {
    return new RtfInfo( "{" + RtfControlWords.INFO_NUMBER_OF_WORDS + numberOfWords + "}" );
  }

  /**
   * Sets the number of pages in this document (for use by a document-management utility).
   *
   * @param numberOfPages Number of pages.
   * @return New RtfInfo object.
   */
  public static RtfInfo numberOfPages( int numberOfPages ) {
    return new RtfInfo( "{" + RtfControlWords.INFO_NUMBER_OF_PAGES + numberOfPages + "}" );
  }
}
