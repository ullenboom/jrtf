package com.tutego.jrtf;

/**
 * Renders package-private RTF-producing objects to a String for test assertions,
 * without adding any public API surface to the main sources.
 */
final class TestSupport {
  private TestSupport() {}

  static String rtf( RtfText text ) {
    StringBuilder sb = new StringBuilder( 1024 );
    text.rtf( new RtfOutput( sb ) );
    return sb.toString();
  }

  static String rtf( RtfPara para, boolean withEndingPar ) {
    StringBuilder sb = new StringBuilder( 1024 );
    para.rtf( new RtfOutput( sb ), withEndingPar );
    return sb.toString();
  }
}
