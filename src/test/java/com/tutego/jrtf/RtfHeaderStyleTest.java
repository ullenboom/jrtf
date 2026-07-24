package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RtfHeaderStyleTest {

  @Test void builtinsHaveFixedIdsAndAreStableAcrossCalls() {
    assertThat( RtfHeaderStyle.NORMAL.getId() ).isEqualTo( 0 );
    assertThat( RtfHeaderStyle.HEADER_1.getId() ).isEqualTo( 1 );
    assertThat( RtfHeaderStyle.HEADER_5.getId() ).isEqualTo( 5 );
    assertThat( RtfHeaderStyle.builtins()[ 0 ] ).isSameAs( RtfHeaderStyle.NORMAL );
    assertThat( RtfHeaderStyle.builtins() ).isNotSameAs( RtfHeaderStyle.builtins() );
  }

  @Test void customStyleRendererIsNotEvaluatedUntilToString() {
    boolean[] called = { false };
    RtfHeaderStyle style = RtfHeaderStyle.custom( "S", out -> { called[ 0 ] = true; out.append( "\\sbasedon0" ); } );
    assertThat( called[ 0 ] ).isFalse();
    Rtf.rtf().headerStyles( style );
    assertThat( called[ 0 ] ).isFalse();
    style.toString();
    assertThat( called[ 0 ] ).isTrue();
  }

  @Test void customStyleRendersNameAndRendererOutput() {
    RtfHeaderStyle style = RtfHeaderStyle.custom( "MyStyle", out -> out.append( "\\sbasedon0" ) );
    Rtf.rtf().headerStyles( style );
    assertThat( style.toString() ).isEqualTo( "{\\s0 MyStyle;\\sbasedon0}" );
  }

  @Test void customStyleRejectsMissingName() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfHeaderStyle.custom( "", out -> {} ) );
    assertThatIllegalArgumentException().isThrownBy( () -> RtfHeaderStyle.custom( null, out -> {} ) );
  }

  @Test void builderComposesBasedOnFontAndAlignment() {
    RtfHeaderStyle style = RtfHeaderStyle.builder( "Built" )
        .basedOn( RtfHeaderStyle.NORMAL ).font( 0 ).fontSize( 24 ).bold().alignCentered().build();
    Rtf.rtf().headerStyles( style );
    assertThat( style.toString() ).isEqualTo( "{\\s0 \\sbasedon0\\f0\\fs24\\b\\qc Built;}" );
  }

  @Test void builderRejectsMissingName() {
    assertThatIllegalArgumentException().isThrownBy( () -> RtfHeaderStyle.builder( "" ) );
  }

  @Test void assignedIdIsNotOverwrittenOnSecondRegistration() {
    RtfHeaderStyle style = RtfHeaderStyle.custom( "S", out -> {} );
    Rtf.rtf().headerStyles( style );
    int firstId = style.getId();
    Rtf.rtf().headerStyles( style ); // register with a different, fresh document
    assertThat( style.getId() ).isEqualTo( firstId );
  }
}
