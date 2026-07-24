package com.tutego.jrtf.office;

import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfCell;
import com.tutego.jrtf.RtfHeaderFont;
import com.tutego.jrtf.RtfHeaderStyle;
import com.tutego.jrtf.RtfList;
import com.tutego.jrtf.RtfPara;
import com.tutego.jrtf.RtfPicture;
import com.tutego.jrtf.RtfText;
import com.tutego.jrtf.RtfTextPara;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.tutego.jrtf.RtfDocfmt.A4;
import static com.tutego.jrtf.RtfDocfmt.defaultTab;
import static com.tutego.jrtf.RtfDocfmt.footnoteNumberingArabic;
import static com.tutego.jrtf.RtfDocfmt.revisionMarking;
import static com.tutego.jrtf.RtfDocfmt.widowOrphanControl;
import static com.tutego.jrtf.RtfHeader.color;
import static com.tutego.jrtf.RtfHeader.font;
import static com.tutego.jrtf.RtfInfo.author;
import static com.tutego.jrtf.RtfInfo.comment;
import static com.tutego.jrtf.RtfInfo.creatim;
import static com.tutego.jrtf.RtfInfo.doccomm;
import static com.tutego.jrtf.RtfInfo.keywords;
import static com.tutego.jrtf.RtfInfo.numberOfPages;
import static com.tutego.jrtf.RtfInfo.numberOfWords;
import static com.tutego.jrtf.RtfInfo.operator;
import static com.tutego.jrtf.RtfInfo.printim;
import static com.tutego.jrtf.RtfInfo.revtim;
import static com.tutego.jrtf.RtfInfo.subject;
import static com.tutego.jrtf.RtfInfo.title;
import static com.tutego.jrtf.RtfInfo.version;
import static com.tutego.jrtf.RtfFields.pageNumberField;
import static com.tutego.jrtf.RtfFields.sectionPagesField;
import static com.tutego.jrtf.RtfFields.tableOfContentsField;
import static com.tutego.jrtf.RtfPara.ol;
import static com.tutego.jrtf.RtfPara.p;
import static com.tutego.jrtf.RtfPara.row;
import static com.tutego.jrtf.RtfPara.ul;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.beginningPageNumber;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.columns;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.footerOnAllPages;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.headerForAllPages;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.pageNumberDecimal;
import static com.tutego.jrtf.RtfSectionFormatAndHeaderFooter.sectionFormatting;
import static com.tutego.jrtf.RtfText.bold;
import static com.tutego.jrtf.RtfText.bookmark;
import static com.tutego.jrtf.RtfText.capitals;
import static com.tutego.jrtf.RtfText.color;
import static com.tutego.jrtf.RtfText.currentDate;
import static com.tutego.jrtf.RtfText.doubleUnderline;
import static com.tutego.jrtf.RtfText.expand;
import static com.tutego.jrtf.RtfText.fontSize;
import static com.tutego.jrtf.RtfText.footnote;
import static com.tutego.jrtf.RtfText.hidden;
import static com.tutego.jrtf.RtfText.hyperlink;
import static com.tutego.jrtf.RtfText.hyperlinkToBookmark;
import static com.tutego.jrtf.RtfText.italic;
import static com.tutego.jrtf.RtfText.kerning;
import static com.tutego.jrtf.RtfText.smallCapitals;
import static com.tutego.jrtf.RtfText.strikethru;
import static com.tutego.jrtf.RtfText.subscriptBy;
import static com.tutego.jrtf.RtfText.superscriptBy;
import static com.tutego.jrtf.RtfText.underline;
import static com.tutego.jrtf.RtfText.wordUnderline;
import static com.tutego.jrtf.RtfUnit.CM;
import static com.tutego.jrtf.RtfUnit.TWIPS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Builds a "kitchen sink" RTF document exercising more or less the whole public jRTF API and
 * verifies it via a real, headlessly-invoked office suite (see {@link OfficeAvailable}) instead
 * of just checking the produced string. Compiling correct-looking RTF and having a real word
 * processor actually accept it are two different things; this is the one test in the suite that
 * checks the latter.
 * <p>
 * <b>What "no error" actually means here, and why it's not just an exit code check:</b> an
 * experiment while writing this test (see {@link #corruptedDocumentIsRejectedByOffice()}) showed
 * that {@code soffice --headless --convert-to} is extremely forgiving: garbage that doesn't even
 * look like RTF (empty files, random binary data, or plain text with an {@code .rtf} extension)
 * is silently accepted with exit code 0, because LibreOffice's importer just falls back to
 * treating unrecognized content as plain text. It only produces a real, non-zero-exit rejection
 * when a file <em>starts</em> like well-formed RTF (a valid {@code {\rtf1...}} header) and then
 * breaks down structurally &mdash; e.g. a truncated file, which is exactly the kind of bug this
 * library could plausibly produce. So "loads without error" is checked here as: the conversion
 * process exits with code 0, <em>and</em> the extracted text actually contains content that was
 * only present in the source document (a round-trip check), not merely that some output file
 * happened to appear.
 */
@OfficeAvailable
class RtfOfficeIntegrationTest {

  private static final String MARKER = "JRTF_OFFICE_ROUNDTRIP_MARKER_7f3ac1";

  @TempDir
  Path tempDir;

  @Test
  void kitchenSinkDocumentIsAcceptedByOffice() throws Exception {
    Path rtfFile = tempDir.resolve( "kitchen-sink.rtf" );
    writeDocument( buildKitchenSinkDocument(), rtfFile );

    ConversionResult result = convertToPlainText( rtfFile, tempDir.resolve( "out-ok" ) );

    assertThat( result.exitCode )
        .as( "soffice should accept a document built entirely from the public jRTF API; stdout=%s stderr=%s",
            result.stdout, result.stderr )
        .isZero();
    assertThat( result.outputText )
        .as( "the converted text should still contain content only present in our document (round-trip check)" )
        .contains( MARKER );
  }

  /**
   * Control test proving the detection mechanism in {@link #kitchenSinkDocumentIsAcceptedByOffice()}
   * actually detects something: take the same well-formed document and cut it off well before any
   * closing braces are reached (simulating e.g. a bug that stops writing mid-document, or a
   * truncated file transfer) and confirm office really does reject it, rather than the test always
   * passing regardless of content. See the class Javadoc for why a genuinely non-RTF-looking file
   * would *not* have worked for this.
   */
  @Test
  void corruptedDocumentIsRejectedByOffice() throws Exception {
    Path wellFormed = tempDir.resolve( "well-formed.rtf" );
    writeDocument( buildKitchenSinkDocument(), wellFormed );

    byte[] bytes = Files.readAllBytes( wellFormed );
    Path truncated = tempDir.resolve( "truncated.rtf" );
    Files.write( truncated, Arrays.copyOf( bytes, bytes.length / 20 ) );

    ConversionResult result = convertToPlainText( truncated, tempDir.resolve( "out-broken" ) );

    assertThat( result.exitCode )
        .as( "a truncated RTF file should be rejected, not silently accepted; stdout=%s stderr=%s",
            result.stdout, result.stderr )
        .isNotZero();
    assertThat( result.stderr ).containsIgnoringCase( "could not be loaded" );
  }

  // Document construction

  private static Rtf buildKitchenSinkDocument() throws IOException {
    RtfList bullets = RtfList.bulleted().indent( 0.5, CM );
    RtfList numbers = RtfList.numbered( RtfList.NumberFormat.DECIMAL )
                             .level( 1, RtfList.NumberFormat.LOWER_LETTER );

    return Rtf.rtf()
        .header(
            color( 0xCC, 0x00, 0x00 ).at( 1 ),
            color( 0x00, 0x80, 0x00 ).at( 2 ),
            color( 0x00, 0x00, 0xCC ).at( 3 ),
            font( RtfHeaderFont.TIMES_ROMAN ).at( 0 ),
            font( RtfHeaderFont.ARIAL ).at( 1 )
        )
        .headerStyles( RtfHeaderStyle.builtins() )
        .lists( bullets, numbers )
        .info(
            title( "jRTF Office Round-Trip Test Document" ),
            author( "jRTF test suite" ),
            subject( "Exercising more or less the whole public API" ),
            keywords( "jrtf rtf test" ),
            comment( "Generated by RtfOfficeIntegrationTest" ),
            operator( "jrtf" ),
            doccomm( "See RtfOfficeIntegrationTest for how this was generated" ),
            version( 1 ),
            numberOfWords( 250 ),
            numberOfPages( 2 ),
            creatim( 2026, 7, 23, 10, 0, 0 ),
            revtim( 2026, 7, 23, 11, 0, 0 ),
            printim( 2026, 7, 23, 12, 0, 0 )
        )
        .documentFormatting(
            A4,
            defaultTab( 1, CM ),
            widowOrphanControl(),
            revisionMarking(),
            footnoteNumberingArabic()
        )
        .section(
            sectionFormatting(
                headerForAllPages( p( "jRTF round-trip test - ", currentDate() ) ),
                footerOnAllPages( p( "Page ", pageNumberField(), " of ", sectionPagesField() ) ),
                pageNumberDecimal(),
                beginningPageNumber( 1 )
            ),
            p( "Marker: " + MARKER ),

            p( RtfHeaderStyle.HEADER_1, "1. Character formatting" ),
            p( bold( "bold" ), " ", italic( "italic" ), " ", underline( "underline" ), " ",
               doubleUnderline( "double underline" ), " ", wordUnderline( "word underline" ) ),
            p( strikethru( "strikethrough" ), " ", smallCapitals( "small caps" ), " ",
               capitals( "all caps" ), " ", hidden( "hidden text" ) ),
            p( subscriptBy( 6, "subscript" ), " ", superscriptBy( 6, "superscript" ), " ",
               kerning( 24, "kerned" ), " ", expand( 20, "expanded" ) ),
            p( fontSize( 32, "bigger text" ), " ",
               color( 1, "red text" ), " ", color( 2, "green text" ), " ", color( 3, "blue text" ) ),

            p( RtfHeaderStyle.HEADER_1, "2. Paragraph formatting" ),
            p( "centered, first-line indented" ).alignCentered().indentFirstLine( 0.25, CM ),
            p( "right aligned" ).alignRight(),
            p( "justified text that should wrap across the line to demonstrate justification" ).alignJustified(),
            p( "bordered and shaded paragraph" )
                .topBorder( RtfTextPara.BorderStyle.DOUBLE, 20, TWIPS, 2 )
                .bottomBorder( RtfTextPara.BorderStyle.SINGLE )
                .backgroundColor( 2 ),
            p( "1\t2\t3" ).tab( 3, CM ).tab( RtfTextPara.TabKind.CENTER, RtfTextPara.TabLead.DOTS, 9, CM ),

            p( RtfHeaderStyle.HEADER_1, "3. Lists" ),
            ul( "legacy bullet item 1" ),
            ul( "legacy bullet item 2" ),
            ol( "legacy ordered item 1" ),
            ol( "legacy ordered item 2" ),
            p( "list-table bullet item 1" ).list( bullets, 0 ),
            p( "list-table bullet item 2" ).list( bullets, 0 ),
            p( "list-table numbered item 1" ).list( numbers, 0 ),
            p( "list-table numbered sub-item a" ).list( numbers, 1 ),

            p( RtfHeaderStyle.HEADER_1, "4. Tables" ),
            row( "Number", "Square" ),
            row( "1", "1" ),
            row( "2", "4" ),
            row(
                RtfCell.cell( bold( "Product" ) ).width( 6, CM ).backgroundColor( 1 ).allBorders(),
                RtfCell.cell( bold( "Price" ) ).width( 3, CM ).backgroundColor( 1 ).alignRight().allBorders()
            ),
            row(
                RtfCell.cell( "Coffee" ).width( 6, CM ).allBorders(),
                RtfCell.cell( "2.50" ).width( 3, CM ).alignRight().allBorders()
            ),

            p( RtfHeaderStyle.HEADER_1, "5. Footnotes and fields" ),
            p( "See this footnote", footnote( "This is a footnote." ), "." ),
            p( "Page ", pageNumberField(), " of ", sectionPagesField() ),
            p( tableOfContentsField() ),

            p( RtfHeaderStyle.HEADER_1, "6. Bookmarks and hyperlinks" ),
            p( bookmark( "TARGET", "This paragraph is the bookmark target." ) ),
            p( hyperlinkToBookmark( "TARGET", RtfPara.p( "Jump to the bookmark above" ) ) ),
            p( hyperlink( "https://example.com", RtfPara.p( "External hyperlink" ) ) ),

            p( RtfHeaderStyle.HEADER_1, "7. Picture" ),
            p( RtfText.picture( new ByteArrayInputStream( tinyPng() ) )
                      .size( 2, 2, CM ).type( RtfPicture.PictureType.PNG ) )
        )
        .section(
            sectionFormatting( columns( 2 ) ),
            p( RtfHeaderStyle.HEADER_1, "8. Second section with two columns" ),
            p( "This paragraph lives in a second section formatted with two columns, to exercise "
             + "multi-section documents and section-level formatting." )
        );
  }

  private static void writeDocument( Rtf rtf, Path target ) throws IOException {
    try ( Writer writer = Files.newBufferedWriter( target, StandardCharsets.ISO_8859_1 ) ) {
      rtf.out( writer );
    }
  }

  /** A minimal, valid 4x4 PNG, generated at test time instead of checked in as a binary fixture. */
  private static byte[] tinyPng() throws IOException {
    BufferedImage image = new BufferedImage( 4, 4, BufferedImage.TYPE_INT_RGB );
    for ( int x = 0; x < 4; x++ )
      for ( int y = 0; y < 4; y++ )
        image.setRGB( x, y, (x + y) % 2 == 0 ? 0xFF0000 : 0x0000FF );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write( image, "png", out );
    return out.toByteArray();
  }

  // Office invocation

  private static final class ConversionResult {
    final int exitCode;
    final String stdout;
    final String stderr;
    final String outputText;

    ConversionResult( int exitCode, String stdout, String stderr, String outputText ) {
      this.exitCode = exitCode;
      this.stdout = stdout;
      this.stderr = stderr;
      this.outputText = outputText;
    }
  }

  private static ConversionResult convertToPlainText( Path rtfFile, Path outDir ) throws IOException, InterruptedException {
    Path soffice = OfficeAvailableCondition.findSoffice();
    if ( soffice == null )
      throw new IllegalStateException( "soffice not found even though @OfficeAvailable enabled this test" );

    Files.createDirectories( outDir );
    Path stdoutFile = outDir.resolve( "stdout.txt" );
    Path stderrFile = outDir.resolve( "stderr.txt" );

    // Deliberately no "-env:UserInstallation=..." override: an experiment showed that combining
    // it with a deeply nested path can crash soffice.bin outright (a LibreOffice/Windows path
    // issue unrelated to RTF content), which is strictly worse than the default profile here.
    ProcessBuilder pb = new ProcessBuilder(
        soffice.toString(), "--headless", "--norestore",
        "--convert-to", "txt:Text",
        "--outdir", outDir.toString(),
        rtfFile.toString() );
    pb.redirectOutput( stdoutFile.toFile() );
    pb.redirectError( stderrFile.toFile() );

    Process process = pb.start();
    boolean finished = process.waitFor( 120, TimeUnit.SECONDS );
    if ( !finished ) {
      process.destroyForcibly();
      throw new IllegalStateException( "soffice did not finish within 120 seconds" );
    }

    String stdout = readFile( stdoutFile );
    String stderr = readFile( stderrFile );

    Path expectedOutput = outDir.resolve( baseNameWithoutExtension( rtfFile ) + ".txt" );
    String outputText = Files.exists( expectedOutput ) ? readFile( expectedOutput ) : "";

    return new ConversionResult( process.exitValue(), stdout, stderr, outputText );
  }

  private static String readFile( Path file ) throws IOException {
    return new String( Files.readAllBytes( file ), StandardCharsets.UTF_8 );
  }

  private static String baseNameWithoutExtension( Path file ) {
    String name = file.getFileName().toString();
    int dot = name.lastIndexOf( '.' );
    return dot < 0 ? name : name.substring( 0, dot );
  }
}
