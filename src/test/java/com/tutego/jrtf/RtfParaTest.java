package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfParaTest {

  @Test void pWithoutStyleUsesNormal() {
    assertThat( rtf( RtfPara.p( "Hello" ), true ) ).isEqualTo( "{\\s0 Hello\\par}\n" );
  }

  @Test void pWithoutEndingPar() {
    assertThat( rtf( RtfPara.p( "Hello" ), false ) ).isEqualTo( "{\\s0 Hello}\n" );
  }

  @Test void pWithStyle() {
    assertThat( rtf( RtfPara.p( RtfHeaderStyle.HEADER_1, "Hi" ), true ) ).isEqualTo( "{\\s1 Hi\\par}\n" );
  }

  @Test void pWithNoTextsIsJustPar() {
    assertThat( rtf( RtfPara.p(), true ) ).isEqualTo( "\\par" );
  }

  @Test void pardIncludesParagraphDefaults() {
    assertThat( rtf( RtfPara.pard( RtfText.text( "Hi" ) ), true ) ).isEqualTo( "{\\pard\\s0 Hi\\par}\n" );
  }

  @Test void pardWithNoTexts() {
    assertThat( rtf( RtfPara.pard(), true ) ).isEqualTo( "\\pard\\par" );
  }

  @Test void olDecimalListItem() {
    assertThat( rtf( RtfPara.ol( "Item" ), true ) ).isEqualTo(
        "{\\pard{\\pntext\\tab}{\\*\\pn\\pnlvlbody\\pndec\\pnstart1\\pnindent360{\\pntxta.}}\\fi-360\\li360 Item\\par}" );
  }

  @Test void olWithNumberingSchemeAndStart() {
    assertThat( rtf( RtfPara.ol( RtfPara.ListNumbering.UPPER_ROMAN, 3, "Item" ), true ) ).isEqualTo(
        "{\\pard{\\pntext\\tab}{\\*\\pn\\pnlvlbody\\pnucrm\\pnstart3\\pnindent360{\\pntxta.}}\\fi-360\\li360 Item\\par}" );
  }

  @Test void olWithoutEndingPar() {
    // ol() consumer always writes \par for correctness; the withEndingPar flag is
    // handled by the framework for RtfTextPara, not for self-contained RtfPara.of() paragraphs.
    assertThat( rtf( RtfPara.ol( "Item" ), false ) ).contains( "Item\\par" );
  }

  @Test void olRejectsNullNumbering() {
    assertThatExceptionOfType( IllegalArgumentException.class )
        .isThrownBy( () -> RtfPara.ol( null, 1, "x" ) );
  }

  @Test void ulWrapsTextWithBulletAndHangingIndent() {
    assertThat( rtf( RtfPara.ul( "Item" ), true ) ).isEqualTo(
        "{\\pard{\\pntext\\bullet\\tab}{\\*\\pn\\pnlvlblt\\pnf1\\pnindent0{\\pntxtb\\bullet}}\\fi-200\\li200 Item\\par}" );
  }

  @Test void hangingUlWrapsTextWithBulletAndIndent() {
    assertThat( rtf( RtfPara.hangingUl( RtfText.text( "Item" ), 0, RtfUnit.TWIPS, 200, RtfUnit.TWIPS, 0, RtfUnit.TWIPS ),
                     true ) )
        .isEqualTo( "{\\li200\\fi-200{\\bullet\\tab}Item\\par}" );
  }

  @Test void rowRendersCellsWithinTableRow() {
    // \cellx is the cumulative right boundary in twips (default 1440 per cell), not the cell index.
    assertThat( rtf( RtfPara.row( "a", "b" ), true ) ).isEqualTo(
        "{\\trowd\\trautofit1\\intbl\n\\clcbpat0\\cellx1440\n\\clcbpat0\\cellx2880\n{\\s0 a}\n\\cell\n{\\s0 b}\n\\cell\n\\row}" );
  }

  @Test void rowRequiresAtLeastOneCell() {
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfPara.rowWithBackgroundColor( 0, (Object[]) null ) );
  }
}
