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
 * Utility methods for building RTF fields &mdash; the {@code \field} / {@code \fldinst} /
 * {@code \fldrslt} construct plus common field types such as hyperlinks, page numbers,
 * and table of contents.
 * <p>
 * The basic field building blocks ({@link RtfText#field(RtfPara, RtfPara)} etc.) live on
 * {@link RtfText}, while the field-instruction-language constants and switches reside here
 * because the field-instruction language is a separate mini-language from RTF control words.
 */
public class RtfFields {
  private RtfFields() {}

  // ---- Field-instruction-language constants ----

  /** Hyperlink bookmark switch ({@code \l}) inside a {@code \fldinst} destination. */
  static final String FIELD_SWITCH_HYPERLINK_BOOKMARK = "\\\\l";

  /** Date-format picture switch ({@code \@}) inside a {@code \fldinst} destination. */
  static final String FIELD_SWITCH_DATE_FORMAT = "\\@";

  /** Instruction text for a Table of Contents field. */
  static final String TOC_FIELD_INSTRUCTION = "TOC \\\\f \\\\h \\\\u \\\\o \"1-5\" ";

  /**
   * Inserts a field with the default string &quot;Refresh &gt;F9&lt;&quot;.
   *
   * @param fieldInstructions Field instructions.
   * @return {@link RtfText} which represents a field.
   */
  public static RtfText field( RtfPara fieldInstructions ) {
    return RtfText.field( fieldInstructions, RtfPara.p( "Refresh 'F9'" ) );
  }

  /**
   * Inserts a field with the default string &quot;Refresh &gt;F9&lt;&quot;.
   *
   * @param fieldInstructions Field instructions.
   * @return {@link RtfText} which represents a field.
   */
  public static RtfText field( String fieldInstructions ) {
    return field( RtfPara.p( fieldInstructions ) );
  }

  /**
   * Writes out a time field in a given format.
   *
   * @param format Format of the time, e.g. "HH:MM"
   * @return {@link RtfText} which represents a field with time.
   */
  public static RtfText timeField( String format ) {
    format = "time " + FIELD_SWITCH_DATE_FORMAT + " \"" + format + "\"";

    return field( format );
  }

  /**
   * Writes field for current pages.
   *
   * @return {@link RtfText} which represents a field with page number.
   */
  public static RtfText pageNumberField() {
    return field( "PAGE" );
  }

  /**
   * Writes field for total number of pages in a section.
   *
   * @return {@link RtfText} which represents a field with totel page numbers.
   */
  public static RtfText sectionPagesField() {
    return field( "SECTIONPAGES" );
  }

  /**
   * Writes field for the author of this document.
   *
   * @return {@link RtfText} which represents a field with author.
   */
  public static RtfText authorField() {
    return field( "AUTHOR" );
  }

  /**
   * Writes field for table of contents.
   *
   * @return {@link RtfText} which represents a field with TOC.
   */
  public static RtfText tableOfContentsField() {
    return field( TOC_FIELD_INSTRUCTION );
  }
}
