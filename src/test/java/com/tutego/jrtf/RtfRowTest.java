package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.RtfCell.cell;
import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;

class RtfRowTest {

  @Test void topAndBottomCellBordersApplyToEveryCell() {
    String out = rtf( RtfPara.row( cell( "a" ), cell( "b" ) ).topCellBorder().bottomCellBorder(), true );
    // row-level border definitions are repeated before every cell boundary
    assertThat( out ).contains( "\\clbrdrt\\brdrs\\clbrdrb\\brdrs\\cellx1440",
                                "\\clbrdrt\\brdrs\\clbrdrb\\brdrs\\cellx2880" );
  }

  @Test void leftAndRightCellBorders() {
    String out = rtf( RtfPara.row( cell( "a" ) ).leftCellBorder().rightCellBorder(), true );
    assertThat( out ).contains( "\\clbrdrl\\brdrs", "\\clbrdrr\\brdrs" );
  }

  @Test void cellHeightAndCellSpaceAreEmitted() {
    String out = rtf( RtfPara.row( cell( "a" ) ).cellHeight( 1, RtfUnit.CM ).cellSpace( 2, RtfUnit.MM ), true );
    assertThat( out ).contains( "\\trrh567", "\\trgaph113" );
  }

  @Test void rightToLeftRow() {
    String out = rtf( RtfPara.row( cell( "a" ) ).rightToLeft(), true );
    assertThat( out ).contains( "\\taprtl" );
  }

  @Test void autoFitRow() {
    String out = rtf( RtfPara.row( cell( "a" ) ).autoFit(), true );
    assertThat( out ).contains( "\\trautofit1" );
  }

  @Test void repeatAsHeaderRowEmitsTrhdr() {
    String out = rtf( RtfPara.row( cell( "a" ) ).repeatAsHeaderRow(), true );
    assertThat( out ).contains( "\\trhdr" );
  }

  @Test void cellMarginsUseCorrectPaddingControlWords() {
    String out = rtf( RtfPara.row( cell( "a" ) )
                          .topCellMargin( 1, RtfUnit.MM )
                          .bottomCellMargin( 2, RtfUnit.MM )
                          .leftCellMargin( 3, RtfUnit.MM )
                          .rightCellMargin( 4, RtfUnit.MM ), true );
    assertThat( out ).contains( "\\trpaddft3\\trpaddt57", "\\trpaddfb3\\trpaddb113",
                                "\\trpaddfl3\\trpaddl170", "\\trpaddfr3\\trpaddr227" );
  }
}
