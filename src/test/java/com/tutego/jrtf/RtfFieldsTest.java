package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;

class RtfFieldsTest {

  @Test void fieldUsesDefaultRefreshResult() {
    assertThat( rtf( RtfFields.field( "INSTR" ) ) )
        .isEqualTo( "{\\field{\\*\\fldinst {\\s0 INSTR}\n}{\\fldrslt {\\s0 Refresh 'F9'}\n}}" );
  }

  @Test void timeFieldWrapsFormatInFieldInstruction() {
    assertThat( rtf( RtfFields.timeField( "HH:MM" ) ) )
        .isEqualTo( "{\\field{\\*\\fldinst {\\s0 time \\\\@ \"HH:MM\"}\n}{\\fldrslt {\\s0 Refresh 'F9'}\n}}" );
  }

  @Test void pageNumberFieldUsesPageInstruction() {
    assertThat( rtf( RtfFields.pageNumberField() ) )
        .isEqualTo( "{\\field{\\*\\fldinst {\\s0 PAGE}\n}{\\fldrslt {\\s0 Refresh 'F9'}\n}}" );
  }
}
