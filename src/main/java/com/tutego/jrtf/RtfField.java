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

import org.jspecify.annotations.Nullable;

// NOTE: @Nullable on field modifier types avoided due to javac issue with inner-class annotations

/**
 * Represents a single RTF field instruction together with its optional
 * recent result and field modifiers.
 * <p>
 * Standard fields are pre-defined as constants; custom fields can be built
 * with {@link #of(String)} and then customized with {@link #withSwitch(String)},
 * {@link #withResult(RtfPara)}, and {@link #withModifier(RtfText.FieldModifier)}.
 * <p>
 * The class is immutable: {@code with*} methods return a new instance.
 * Rendering is done lazily via {@link RtfText#field(RtfField)}.
 */
public final class RtfField {

  /** The raw field instruction, e.g. {@code "PAGE"} or {@code "HYPERLINK \"url\""}. */
  final String instruction;

  /** Optional recent result, shown when the field is not updated. */
  @Nullable final RtfPara recentResult;

  /** Optional field modifier. */
  final RtfText.FieldModifier modifier;

  private RtfField( String instruction, @Nullable RtfPara recentResult,
                    RtfText.FieldModifier modifier ) {
    this.instruction = instruction;
    this.recentResult = recentResult;
    this.modifier = modifier;
  }

  // ---- Standard field constants ----

  /** Current page number. */
  public static final RtfField PAGE       = new RtfField( "PAGE", null, null );
  /** Total number of pages. */
  public static final RtfField NUMPAGES   = new RtfField( "NUMPAGES", null, null );
  /** Current date. */
  public static final RtfField DATE       = new RtfField( "DATE", null, null );
  /** Current time. */
  public static final RtfField TIME       = new RtfField( "TIME", null, null );
  /** Document author from properties. */
  public static final RtfField AUTHOR     = new RtfField( "AUTHOR", null, null );
  /** Document title from properties. */
  public static final RtfField TITLE      = new RtfField( "TITLE", null, null );
  /** Document subject from properties. */
  public static final RtfField SUBJECT    = new RtfField( "SUBJECT", null, null );
  /** Number of words in the document. */
  public static final RtfField NUMWORDS   = new RtfField( "NUMWORDS", null, null );
  /** Number of characters in the document. */
  public static final RtfField NUMCHARS   = new RtfField( "NUMCHARS", null, null );
  /** File name. */
  public static final RtfField FILENAME   = new RtfField( "FILENAME", null, null );
  /** File size in bytes. */
  public static final RtfField FILESIZE   = new RtfField( "FILESIZE", null, null );
  /** Table of contents. */
  public static final RtfField TOC        = new RtfField( "TOC", null, null );
  /** Index entry. */
  public static final RtfField INDEX      = new RtfField( "INDEX", null, null );

  // ---- Well-known field switches ----

  /** Merge formatting when the field is updated. */
  public static final String MERGEFORMAT  = "\\* MERGEFORMAT";
  /** Merge character formatting. */
  public static final String CHARFORMAT   = "\\* CHARFORMAT";
  /** Convert field result to uppercase. */
  public static final String UPPER        = "\\* Upper";
  /** Convert field result to lowercase. */
  public static final String LOWER        = "\\* Lower";
  /** First character uppercase, rest lowercase. */
  public static final String FIRSTCAP     = "\\* FirstCap";
  /** Capitalize each word. */
  public static final String CAPS         = "\\* Caps";
  /** Do not update the field. */
  public static final String LOCKED       = "\\!";

  // ---- Factory ----

  /**
   * Creates a custom field with the given instruction text.
   *
   * @param instruction Raw field instruction, e.g. {@code "DOCVARIABLE myVar"}.
   * @return New {@code RtfField}.
   */
  public static RtfField of( String instruction ) {
    if ( instruction == null || instruction.isEmpty() )
      throw new IllegalArgumentException( "Field instruction must not be empty" );
    return new RtfField( instruction, null, null );
  }

  // ---- Builder-style with* methods (return new instance) ----

  /**
   * Appends an RTF switch to this field's instruction.
   *
   * @param rtfSwitch Switch text, e.g. {@link #MERGEFORMAT} or {@code "\\@ \"dd.MM.yyyy\""}.
   * @return New {@code RtfField} with the switch appended.
   */
  public RtfField withSwitch( String rtfSwitch ) {
    if ( rtfSwitch == null )
      throw new IllegalArgumentException( "rtfSwitch must not be null" );
    return new RtfField( instruction + " " + rtfSwitch, recentResult, modifier );
  }

  /**
   * Sets a recent result for this field (shown when not updated).
   *
   * @param result Paragraph to show as recent result.
   * @return New {@code RtfField} with the result set.
   */
  public RtfField withResult( RtfPara result ) {
    return new RtfField( instruction, result, modifier );
  }

  /**
   * Sets a field modifier.
   *
   * @param modifier Field modifier, e.g. {@link RtfText.FieldModifier#LOCKED}.
   * @return New {@code RtfField} with the modifier set.
   */
  public RtfField withModifier( RtfText.FieldModifier modifier ) {
    return new RtfField( instruction, recentResult, modifier );
  }

  // ---- Common convenience methods ----

  /**
   * Returns a copy of this field with {@link #MERGEFORMAT} appended.
   * Equivalent to {@code withSwitch(MERGEFORMAT)}.
   */
  public RtfField mergeFormat() {
    return withSwitch( MERGEFORMAT );
  }

  /**
   * Returns a date/time field with the given picture format.
   * Example: {@code DATE.formatted("\\@ \"dd.MM.yyyy\"")}.
   */
  public RtfField formatted( String pictureSwitch ) {
    return withSwitch( pictureSwitch );
  }
}
