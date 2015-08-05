import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfPara.*;
import java.awt.Desktop;
import java.io.*;
import com.tutego.jrtf.*;

/**
 * Example class showing RTF table abilities.
 */
public class RtfTableDemo
{
  /**
   * Starts application.
   * @param args Program arguments.
   * @throws IOException If something goes wrong during writing.
   */
  public static void main( String... args ) throws IOException
  {
    File out = new File( "out-table.rtf" );
    
    final RtfHeader red   = RtfHeader.color( 0xff, 0, 0 ).at( 1 );
    final RtfHeader green = RtfHeader.color( 0, 0xff, 0 ).at( 2 );
    final RtfHeader blue  = RtfHeader.color( 0, 0, 0xff ).at( 3 );
    final RtfHeader black = RtfHeaderColor.BLACK.at( 4 );

    rtf().header( red, green, blue, black )
         .section(
           row( p( "ROW WITHOUT A BACKGROUND COLOR" ).cellWidth( 10, RtfUnit.CM ) ),
           rowWithBackgroundColor( 0, p( "DEFAULT COLOR (INDEX 0)" ) ),
           rowWithBackgroundColor( 1, p( "RED" ) ),
           rowWithBackgroundColor( 2, "GREEN" ),
           rowWithBackgroundColor( 3, RtfText.text( "BLUE" ) ),
           rowWithBackgroundColor( 4, RtfText.text( "BLACK" ) ),
           rowWithBackgroundColor( 5, p( "COLOR NOT FOUND" ) )
           )
    	.out( new FileWriter( out ) );   			

    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}
