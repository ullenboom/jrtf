package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RtfTest {

  @Test void outClosesTheAppendableWhenItIsCloseable() {
    boolean[] closed = { false };
    Writer writer = new Writer() {
      @Override public void write( char[] cbuf, int off, int len ) {}
      @Override public void flush() {}
      @Override public void close() { closed[ 0 ] = true; }
    };
    Rtf.rtf().p( "Hi" ).out( writer );
    assertThat( closed[ 0 ] ).as( "out(Appendable) must close a Closeable target" ).isTrue();
  }

  @Test void outWritesTheDocumentToTheAppendable() {
    StringBuilder sb = new StringBuilder();
    Rtf.rtf().p( "Hi" ).out( sb );
    assertThat( sb.toString() ).isEqualTo( Rtf.rtf().p( "Hi" ).toString() );
  }

  @Test void outRejectsNullAppendable() {
    assertThatExceptionOfType( IllegalArgumentException.class )
        .isThrownBy( () -> Rtf.rtf().p( "Hi" ).out( null ) );
  }

  @Test void minimalDocumentHasDefaultFontAndColorTable() {
    assertThat( Rtf.rtf().p( "Hi" ).toString() )
        .isEqualTo( "{\\rtf1\\ansi\\deff0\n{\\fonttbl{\\f0 Times New Roman;}}\n{\\colortbl;}\n{\\s0 Hi\\par}\n}" );
  }

  @Test void styleRendererIsEvaluatedOncePerWriteRegardlessOfParagraphReferenceCount() {
    int[] calls = { 0 };
    RtfHeaderStyle style = RtfHeaderStyle.custom( "S1", out -> {
      calls[ 0 ]++;
      out.append( "\\sbasedon0" );
    } );

    Rtf doc = Rtf.rtf().headerStyles( style );
    for ( int i = 0; i < 5; i++ )
      doc.p( style, "Para " + i );

    assertThat( calls[ 0 ] ).as( "renderer must not run before the document is written" ).isZero();

    doc.toString();
    assertThat( calls[ 0 ] ).as( "renderer must run exactly once per write, not once per paragraph reference" )
        .isEqualTo( 1 );

    doc.toString();
    assertThat( calls[ 0 ] ).as( "a second write evaluates the renderer again (no caching of the produced RTF)" )
        .isEqualTo( 2 );
  }

  @Test void stylesheetTablePrecedesTheBody() {
    RtfHeaderStyle style = RtfHeaderStyle.custom( "S1", out -> out.append( "\\sbasedon0" ) );
    Rtf doc = Rtf.rtf().headerStyles( style );
    doc.p( style, "Para 0" );

    String out = doc.toString();

    assertThat( out ).contains( "\\stylesheet" );
    assertThat( out.indexOf( "\\stylesheet" ) ).isLessThan( out.indexOf( "Para 0" ) );
  }

  @Test void headerFontsAreWrittenInsteadOfDefault() {
    String out = Rtf.rtf().header( RtfHeader.font( "Arial" ) ).p( "Hi" ).toString();
    assertThat( out ).contains( "Arial" ).doesNotContain( "Times New Roman" );
  }

  @Test void colorTableIsWrittenWhenColorsAreRegistered() {
    String out = Rtf.rtf().header( RtfHeader.color( 255, 0, 0 ) ).p( RtfText.color( 0, "Hi" ) ).toString();
    assertThat( out ).contains( "\\colortbl", "\\red255" );
  }

  @Test void multipleSectionsAreSeparatedBySect() {
    Rtf doc = Rtf.rtf();
    doc.section( RtfPara.p( "First" ) );
    doc.section( RtfPara.p( "Second" ) );
    String out = doc.toString();

    assertThat( out ).contains( "First", "\\sect" );
    assertThat( out.indexOf( "First" ) ).isLessThan( out.indexOf( "\\sect" ) );
    assertThat( out.indexOf( "\\sect" ) ).isLessThan( out.indexOf( "Second" ) );
  }
}
