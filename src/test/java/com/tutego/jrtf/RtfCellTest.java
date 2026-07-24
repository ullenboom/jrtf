package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.RtfCell.cell;
import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfCellTest {

  @Test void rowOfCellsUsesCumulativeCellxFromWidths() {
    String out = rtf( RtfPara.row(
        cell( "a" ).width( 1, RtfUnit.INCH ),
        cell( "b" ).width( 2, RtfUnit.INCH ) ), true );
    // 1 inch = 1440 twips, 2 inch = 2880 -> boundaries 1440 and 4320
    assertThat( out ).isEqualTo(
        "{\\trowd\\intbl\n" +
        "\\clftsWidth3\\clwWidth1440\\cellx1440\n" +
        "\\clftsWidth3\\clwWidth2880\\cellx4320\n" +
        "{\\s0 a}\n\\cell\n{\\s0 b}\n\\cell\n\\row}" );
  }

  @Test void cellWithoutWidthUsesDefaultWidthForBoundary() {
    String out = rtf( RtfPara.row( cell( "a" ), cell( "b" ) ), true );
    assertThat( out ).contains( "\\cellx1440", "\\cellx2880" );
  }

  @Test void backgroundColorIsPerCell() {
    String out = rtf( RtfPara.row(
        cell( "a" ).backgroundColor( 1 ),
        cell( "b" ).backgroundColor( 2 ) ), true );
    assertThat( out ).contains( "\\clcbpat1\\cellx", "\\clcbpat2\\cellx" );
  }

  @Test void allBordersEmitsFourCellBorders() {
    String out = rtf( RtfPara.row( cell( "a" ).allBorders() ), true );
    assertThat( out ).contains( "\\clbrdrt\\brdrs", "\\clbrdrb\\brdrs", "\\clbrdrl\\brdrs", "\\clbrdrr\\brdrs" );
  }

  @Test void verticalAlignCenterEmitsControlWord() {
    String out = rtf( RtfPara.row( cell( "a" ).verticalAlignCenter() ), true );
    assertThat( out ).contains( "\\clvertalc" );
  }

  @Test void horizontalMergeEmitsMergeControlWords() {
    String out = rtf( RtfPara.row(
        cell( "a" ).mergeHorizontallyFirst(),
        cell( "b" ).mergeHorizontally() ), true );
    assertThat( out ).contains( "\\clmgf", "\\clmrg" );
  }

  @Test void alignRightAppliesToCellParagraph() {
    String out = rtf( RtfPara.row( cell( "a" ).alignRight() ), true );
    assertThat( out ).contains( "\\qr" );
  }

  @Test void cellWithMultipleParagraphsSeparatesWithParButTerminatesOnceWithCell() {
    String out = rtf( RtfPara.row( cell( RtfPara.p( "a" ), RtfPara.p( "b" ) ) ), true );
    // first paragraph ends with \par, second does not; single \cell terminates the cell
    assertThat( out ).contains( "{\\s0 a\\par}\n{\\s0 b}\n\\cell\n" );
  }

  @Test void cellRejectsEmptyParagraphs() {
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfCell.cell( (RtfPara[]) null ) );
  }

  @Test void rowRejectsNoCells() {
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfPara.row( (RtfCell[]) null ) );
  }
}
