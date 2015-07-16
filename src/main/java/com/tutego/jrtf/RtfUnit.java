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
 * Represents a RTF measurement.
 */
public enum RtfUnit
{
  /** Measurement in twips. A twip is a 1/20 of a point and the internal representation in RTF. */
  TWIPS { @Override public int toTwips( double v ) { return (int) v; } },

  /** Measurement in point. One point is 20 twips. */
  POINT { @Override public int toTwips( double v ) { return (int) (v*20); } },

  /** Measurement in inch. One inch is 1440 twips. */
  INCH { @Override public int toTwips( double v ) { return (int) (v*1440); } },

  /** Measurement in cm. One cm is 566.9 twips (rounded to 567). */
  CM { @Override public int toTwips( double v ) { return (int) Math.round( v * 566.9 ); } },

  /** Measurement in mm. One mm is 56.69 twips (rounded to 57). */
  MM { @Override public int toTwips( double v ) { return (int) Math.round( v * 566.9 / 10. ); } };

  /**
   * Converts to twips.
   * @param v Value to convert to twips.
   * @return Twips.
   */
  public abstract int toTwips( double v );
}
