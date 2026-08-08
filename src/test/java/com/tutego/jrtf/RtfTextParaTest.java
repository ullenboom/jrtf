package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RtfTextParaTest {

  @Test void alignmentAndIndentAreAccumulatedInOrder() {
    RtfTextPara para = RtfPara.p( "x" ).alignCentered().indentLeft( 100, RtfUnit.TWIPS );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\qc\n\\li100\nx\\par}\n" );
  }

  @Test void bugfixedBorderStylesRenderCorrectly() {
    RtfTextPara para = RtfPara.p( "y" )
        .topBorder( RtfTextPara.BorderStyle.DASHED )
        .bottomBorder( RtfTextPara.BorderStyle.HAIRLINE );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\brdrt\\brdrdash\n\\brdrb\\brdrhair\ny\\par}\n" );
  }

  @Test void doubleThicknessShadowedDoubleDottedBordersRenderNotCorrupted() {
    for ( RtfTextPara.BorderStyle style : new RtfTextPara.BorderStyle[]{
        RtfTextPara.BorderStyle.DOUBLE_THICKNESS, RtfTextPara.BorderStyle.SHADOWED,
        RtfTextPara.BorderStyle.DOUBLE, RtfTextPara.BorderStyle.DOTTED } ) {
      String out = rtf( RtfPara.p( "x" ).topBorder( style ), true );
      // The old bug produced a literal backspace control char instead of "\brdr..." - assert it's gone.
      assertThat( out ).doesNotContain( "\b" ).doesNotContain( "\\\b" );
    }
  }

  @Test void tabWithKindAndLead() {
    RtfTextPara para = RtfPara.p( "z" ).tab( RtfTextPara.TabKind.RIGHT, RtfTextPara.TabLead.DOTS, 100, RtfUnit.TWIPS );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\tqr\\tldot\\tx100\nz\\par}\n" );
  }

  @Test void hangingTabUsesLeftAndFirstLineIndent() {
    RtfTextPara para = RtfPara.p( "z" ).tab( RtfTextPara.TabKind.HANGING, 200, RtfUnit.TWIPS );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\li200\\fi-200\nz\\par}\n" );
  }

  @Test void barTabEmitsTbControlWord() {
    RtfTextPara para = RtfPara.p( "z" ).bartab( 300, RtfUnit.TWIPS );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\tb300\nz\\par}\n" );
  }

  @Test void barTabWithLead() {
    RtfTextPara para = RtfPara.p( "z" ).bartab( RtfTextPara.TabLead.DOTS, 300, RtfUnit.TWIPS );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\tldot\\tb300\nz\\par}\n" );
  }

  @Test void levelRejectsNegative() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfPara.p( "x" ).level( -1 ) );
  }

  @Test void bordersRejectNullStyle() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfPara.p( "x" ).topBorder( null ) );
  }

  @Test void borderWithWidthAndColorEmitsBrdrwAndBrdrcf() {
    RtfTextPara para = RtfPara.p( "x" ).topBorder( RtfTextPara.BorderStyle.SINGLE, 1, RtfUnit.TWIPS, 2 );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\brdrt\\brdrs\\brdrw1\\brdrcf2 \nx\\par}\n" );
  }

  @Test void borderWithoutWidthOrColorOmitsBrdrwAndBrdrcf() {
    RtfTextPara para = RtfPara.p( "x" ).topBorder( RtfTextPara.BorderStyle.SINGLE );
    assertThat( rtf( para, true ) ).doesNotContain( "\\brdrw" ).doesNotContain( "\\brdrcf" );
  }

  @Test void backgroundColorEmitsShadingAndBackgroundPatternColor() {
    RtfTextPara para = RtfPara.p( "x" ).backgroundColor( 3 );
    assertThat( rtf( para, true ) ).isEqualTo( "{\\s0 \\shading10000\\cbpat3\nx\\par}\n" );
  }
}
