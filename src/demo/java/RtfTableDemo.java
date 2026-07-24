import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfCell.cell;
import static com.tutego.jrtf.RtfPara.*;
import static com.tutego.jrtf.RtfText.bold;

import java.awt.Desktop;
import java.io.*;

import com.tutego.jrtf.*;

/**
 * Example class showing RTF table abilities.
 */
public class RtfTableDemo {
  /**
   * Starts application.
   *
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException {
    File out = new File( "out-table.rtf" );

    final RtfHeader red = RtfHeader.color( 0xff, 0, 0 ).at( 1 );
    final RtfHeader green = RtfHeader.color( 0, 0xff, 0 ).at( 2 );
    final RtfHeader blue = RtfHeader.color( 0, 0, 0xff ).at( 3 );
    final RtfHeader black = RtfHeaderColor.BLACK.at( 4 );

    rtf().header( red, green, blue, black )
         // Legacy convenience API: one background color per row.
         .section(
             row( p( "ROW WITHOUT A BACKGROUND COLOR" ).cellWidth( 10, RtfUnit.CM ) ),
             rowWithBackgroundColor( 0, p( "DEFAULT COLOR (INDEX 0)" ) ),
             rowWithBackgroundColor( 1, p( "RED" ) ),
             rowWithBackgroundColor( 2, "GREEN" ),
             rowWithBackgroundColor( 3, RtfText.text( "BLUE" ) ),
             rowWithBackgroundColor( 4, RtfText.text( "BLACK" ) ),
             rowWithBackgroundColor( 5, p( "COLOR NOT FOUND" ) )
         )
         // New RtfCell API: every cell fully specified (width, shading, borders, alignment).
         .section(
             row(
                 cell( bold( "Product" ) ).width( 6, RtfUnit.CM ).backgroundColor( 4 ).allBorders(),
                 cell( bold( "Price" ) ).width( 3, RtfUnit.CM ).backgroundColor( 4 ).alignRight().allBorders()
             ),
             row(
                 cell( "Coffee" ).width( 6, RtfUnit.CM ).allBorders(),
                 cell( "2.50" ).width( 3, RtfUnit.CM ).alignRight().allBorders()
             ),
             row(
                 cell( "Tea" ).width( 6, RtfUnit.CM ).backgroundColor( 2 ).allBorders().verticalAlignCenter(),
                 cell( "1.90" ).width( 3, RtfUnit.CM ).alignRight().allBorders()
             )
         )
         .out( new FileWriter( out ) );

    try {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}
