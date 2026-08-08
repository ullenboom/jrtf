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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pre-defined table style, similar to {@link RtfHeaderStyle} for paragraphs.
 * Register with {@link Rtf#tableStyles(RtfTableStyle...)} and reference from
 * a row with {@link RtfRow#tableStyle(RtfTableStyle)}.
 *
 * <pre>{@code
 * RtfTableStyle gridStyle = RtfTableStyle.builder()
 *     .allBorders().rowBandSize( 1 ).build();
 * rtf.tableStyles( gridStyle );
 * rtf.section( row( "A", "B" ).tableStyle( gridStyle ) );
 * }</pre>
 */
public final class RtfTableStyle {

  private int id = -1;
  private final Consumer<RtfOutput> renderer;

  private RtfTableStyle( Consumer<RtfOutput> renderer ) {
    this.renderer = renderer;
  }

  void assignIdIfUnassigned( int nextId ) {
    if ( id < 0 ) id = nextId;
  }

  int getId() { return id; }

  void rtf( RtfOutput out ) {
    renderer.accept( out );
  }

  // ---- Builder ----

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final List<String> definitions = new ArrayList<>();
    private int rowBandSize = 0;
    private int columnBandSize = 0;

    public Builder allBorders() {
      definitions.add( "\\" + RtfControlWords.CELL_BORDER_TOP    + "\\" + RtfControlWords.BORDER_SINGLE );
      definitions.add( "\\" + RtfControlWords.CELL_BORDER_BOTTOM + "\\" + RtfControlWords.BORDER_SINGLE );
      definitions.add( "\\" + RtfControlWords.CELL_BORDER_LEFT   + "\\" + RtfControlWords.BORDER_SINGLE );
      definitions.add( "\\" + RtfControlWords.CELL_BORDER_RIGHT  + "\\" + RtfControlWords.BORDER_SINGLE );
      return this;
    }

    public Builder rowBandSize( int size ) { rowBandSize = size; return this; }
    public Builder columnBandSize( int size ) { columnBandSize = size; return this; }

    public RtfTableStyle build() {
      int rb = rowBandSize, cb = columnBandSize;
      List<String> defs = new ArrayList<>( definitions );
      return new RtfTableStyle( out -> {
        out.open( RtfControlWords.TABLE_STYLE_DEFINITION );
        for ( String def : defs )
          out.append( def );
        if ( rb > 0 )
          out.cw( RtfControlWords.TABLE_STYLE_ROW_BAND_SIZE ).append( rb );
        if ( cb > 0 )
          out.cw( RtfControlWords.TABLE_STYLE_COLUMN_BAND_SIZE ).append( cb );
        out.semi();
        out.close();
      } );
    }
  }
}
