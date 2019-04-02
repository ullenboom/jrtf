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
public class RtfInfo
{
  /** RTF result of this info. */
  String rtf;

  /** Initializes this object. */
  private RtfInfo( String rtf )
  {
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
   * @param subject Subject.
   * @return New RtfInfo object.
   */
  public static RtfInfo subject( String subject )
  {
    // <subject> '{' \subject #PCDATA '}'
    return new RtfInfo( "{\\subject " + Rtf.asRtf( subject ) + "}" );
  }

  /**
   * Sets the title of this document.
   * @param title Title.
   * @return New RtfInfo object.
   */
  public static RtfInfo title( String title )
  {
    // <title> '{' \title #PCDATA '}'
    return new RtfInfo( "{\\title " + Rtf.asRtf( title ) + "}" );
  }

  /**
   * Sets the author of this document.
   * @param author Author.
   * @return New RtfInfo object.
   */
  public static RtfInfo author( String author )
  {
    // <author> '{' \author #PCDATA '}'
    return new RtfInfo( "{\\author " + Rtf.asRtf( author ) + "}" );
  }

  /**
   * Sets the create time of this document.
   * @param year Year.
   * @param month Month.
   * @param dayOfMonth Day of month.
   * @param hour Hour.
   * @param minute Minute.
   * @param second Second.
   * @return New RtfInfo object.
   */
  public static RtfInfo creatim( int year, int month, int dayOfMonth, int hour, int minute, int second )
  {
    // <creatim> '{' \ creatim <time> '}'
    return new RtfInfo( String.format("{\\creatim \\yr%d \\mo%d \\dy%d \\hr%d \\min%d \\sec%d}", year, month, dayOfMonth, hour, minute, second) );
  }
}
