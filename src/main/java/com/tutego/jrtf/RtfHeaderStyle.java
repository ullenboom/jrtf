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
package com.tutego.jrtf;

/**
 * Represents a style sheet definition for the RTF header.
 */
public enum RtfHeaderStyle
{
  /** Default style. */
  NORMAL( 0 ) {
    @Override public String toString() { return "{\\s0 Normal;}"; }
  },

  /** Header 1 Style. */
  HEADER_1( 1 ) {
    @Override public String toString() { return "{\\s1 Heading 1;}"; }
  },

  /** Header 2 Style. */
  HEADER_2( 2 ) {
    @Override public String toString() { return "{\\s2 Heading 2;}"; }
  },

  /** Header 3 Style. */
  HEADER_3( 3 ) {
    @Override public String toString() { return "{\\s3 Heading 3;}"; }
  },

  /** Header 4 Style. */
  HEADER_4( 4 ) {
    @Override public String toString() { return "{\\s4 Heading 4;}"; }
  },

  /** Header 5 Style. */
  HEADER_5( 5 ) {
    @Override public String toString() { return "{\\s5 Heading 5;}"; }
  };

  /** Style sheet id. */
  private int id;

  /**
   * Internal constructor.
   * @param id document style sheet id
   */
  RtfHeaderStyle( int id ) {
    this.id = id;
  }

  /**
   * Returns document style sheet id.
   * @return id
   */
  public int getId() {
    return id;
  }	
}
