package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RtfTextTest {

  @Test void plainText() {
    assertThat( rtf( RtfText.text( "Hello" ) ) ).isEqualTo( "Hello" );
  }

  @Test void nullTextBecomesEmpty() {
    assertThat( rtf( RtfText.text( (String) null ) ) ).isEmpty();
  }

  @Test void textJoinsMultipleObjectsWithoutSpace() {
    assertThat( rtf( RtfText.text( "a", "b" ) ) ).isEqualTo( "ab" );
  }

  @Test void textVarargNullElementIsOmitted() {
    assertThat( rtf( RtfText.text( "a", null, "b" ) ) ).isEqualTo( "ab" );
  }

  @Test void textJoinWithSpaceJoinsNonNullElementsWithSpace() {
    assertThat( rtf( RtfText.textJoinWithSpace( true, "a", "b" ) ) ).isEqualTo( "a b" );
  }

  @Test void textJoinWithSpaceOmitsSpaceAroundNullElement() {
    assertThat( rtf( RtfText.textJoinWithSpace( true, "a", null, "b" ) ) ).isEqualTo( "ab" );
  }

  @Test void bold() {
    assertThat( rtf( RtfText.bold( "Hello" ) ) ).isEqualTo( "{\\b Hello}" );
  }

  @Test void italic() {
    assertThat( rtf( RtfText.italic( "Hello" ) ) ).isEqualTo( "{\\i Hello}" );
  }

  @Test void underline() {
    assertThat( rtf( RtfText.underline( "Hello" ) ) ).isEqualTo( "{\\ul Hello}" );
  }

  @Test void subscript() {
    assertThat( rtf( RtfText.subscript( "Hello" ) ) ).isEqualTo( "{\\sub Hello}" );
  }

  @Test void superscript() {
    assertThat( rtf( RtfText.superscript( "Hello" ) ) ).isEqualTo( "{\\super Hello}" );
  }

  @Test void strikethru() {
    assertThat( rtf( RtfText.strikethru( "Hello" ) ) ).isEqualTo( "{\\strike Hello}" );
  }

  @Test void shadow() {
    assertThat( rtf( RtfText.shadow( "Hello" ) ) ).isEqualTo( "{\\shad Hello}" );
  }

  @Test void font() {
    assertThat( rtf( RtfText.font( 2, "Hello" ) ) ).isEqualTo( "{\\f2 Hello}" );
  }

  @Test void fontSize() {
    assertThat( rtf( RtfText.fontSize( 36, "Hello" ) ) ).isEqualTo( "{\\fs36 Hello}" );
  }

  @Test void fontSizeRejectsNegativeSize() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.fontSize( -1, "Hello" ) );
  }

  @Test void color() {
    assertThat( rtf( RtfText.color( 3, "Hello" ) ) ).isEqualTo( "{\\cf3 Hello}" );
  }

  @Test void backgroundcolor() {
    assertThat( rtf( RtfText.backgroundcolor( 4, "Hello" ) ) ).isEqualTo( "{\\cb4 Hello}" );
  }

  @Test void foregroundAndBackgroundColorTogether() {
    assertThat( rtf( RtfText.color( 3, 4, "Hello" ) ) ).isEqualTo( "{\\cf3\\cb4 Hello}" );
  }

  @Test void backgroundcolorWithStringOverload() {
    assertThat( rtf( RtfText.backgroundcolor( 4, (Object) "Hello" ) ) ).isEqualTo( "{\\cb4 Hello}" );
  }

  @Test void bullet() {
    assertThat( rtf( RtfText.bullet() ) ).isEqualTo( "\\bullet\n" );
  }

  @Test void nonBreakingSpace() {
    assertThat( rtf( RtfText.nonBreakingSpace() ) ).isEqualTo( "\\~\n" );
  }

  @Test void softLineBreak() {
    assertThat( rtf( RtfText.softLineBreak() ) ).isEqualTo( "\\softline\n" );
  }

  @Test void escapingOfSpecialChars() {
    assertThat( rtf( RtfText.text( "a{b}c\\d" ) ) ).isEqualTo( "a\\{b\\}c\\\\d" );
  }

  @Test void footnoteWrapsTextInFootnoteDestination() {
    assertThat( rtf( RtfText.footnote( "Note text" ) ) )
        .isEqualTo( "\\chftn{\\footnote{\\up6\\chftn }{\\s0 Note text}\n}\n" );
  }

  @Test void fieldWithoutModifier() {
    assertThat( rtf( RtfText.field( RtfPara.p( "INSTR" ), RtfPara.p( "RESULT" ) ) ) )
        .isEqualTo( "{\\field{\\*\\fldinst {\\s0 INSTR}\n}{\\fldrslt {\\s0 RESULT}\n}}" );
  }

  @Test void fieldWithModifier() {
    assertThat( rtf( RtfText.field( RtfPara.p( "INSTR" ), null, RtfText.FieldModifier.LOCKED ) ) )
        .isEqualTo( "{\\field\\fldlock{\\*\\fldinst {\\s0 INSTR}\n}{\\fldrslt }}" );
  }

  @Test void fieldRequiresInstructions() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.field( null, null ) );
  }

  @Test void hyperlink() {
    assertThat( rtf( RtfText.hyperlink( "https://example.com", RtfPara.p( "link" ) ) ) )
        .isEqualTo( "{\\field{\\*\\fldinst{HYPERLINK \"https://example.com\"}}{\\fldrslt{\\ul {\\s0 link}\n}}}" );
  }

  @Test void capitals() {
    assertThat( rtf( RtfText.capitals( "abc" ) ) ).isEqualTo( "{\\caps abc}" );
  }

  @Test void hidden() {
    assertThat( rtf( RtfText.hidden( "secret" ) ) ).isEqualTo( "{\\v secret}" );
  }

  @Test void kerning() {
    assertThat( rtf( RtfText.kerning( 24, "abc" ) ) ).isEqualTo( "{\\kerning24 abc}" );
  }

  @Test void kerningRejectsNegativeFontSize() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.kerning( -1, "abc" ) );
  }

  @Test void expandPositive() {
    assertThat( rtf( RtfText.expand( 20, "abc" ) ) ).isEqualTo( "{\\expndtw20 abc}" );
  }

  @Test void expandNegativeCondensesText() {
    assertThat( rtf( RtfText.expand( -20, "abc" ) ) ).isEqualTo( "{\\expndtw-20 abc}" );
  }

  @Test void subscriptBy() {
    assertThat( rtf( RtfText.subscriptBy( 6, "abc" ) ) ).isEqualTo( "{\\dn6 abc}" );
  }

  @Test void subscriptByRejectsNegativeAmount() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.subscriptBy( -1, "abc" ) );
  }

  @Test void superscriptBy() {
    assertThat( rtf( RtfText.superscriptBy( 6, "abc" ) ) ).isEqualTo( "{\\up6 abc}" );
  }

  @Test void superscriptByRejectsNegativeAmount() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.superscriptBy( -1, "abc" ) );
  }

  @Test void bookmarkWrapsContentInStartAndEndDestinations() {
    assertThat( rtf( RtfText.bookmark( "TARGET", "abc" ) ) )
        .isEqualTo( "{\\*\\bkmkstart TARGET}abc{\\*\\bkmkend TARGET}" );
  }

  @Test void bookmarkRejectsNullName() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfText.bookmark( null, "abc" ) );
  }

  @Test void hyperlinkToBookmarkUsesLSwitch() {
    assertThat( rtf( RtfText.hyperlinkToBookmark( "TARGET", RtfPara.p( "link" ) ) ) )
        .isEqualTo( "{\\field{\\*\\fldinst{HYPERLINK \\\\l \"TARGET\"}}{\\fldrslt{\\ul {\\s0 link}\n}}}" );
  }
}
