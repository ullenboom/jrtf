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
    
    final RtfHeader red   = RtfHeader.color( 0xff, 0, 0 ).at( 0 );
    final RtfHeader green = RtfHeader.color( 0, 0xff, 0 ).at( 1 );
    final RtfHeader blue  = RtfHeader.color( 0, 0, 0xff ).at( 2 );

    rtf().header( red, green, blue )
         .section(
           row( p( "ROW WITHOUT A BACKGROUND COLOR" ) )
/*           , row( 0, p( "RED" ).cellWidth( 10, RtfUnit.CM ) ),
           row( 1, "GREEN" ),
           row( 2, RtfText.text( "BLUE" ) ),
           row( 3, p( "COLOR NOT FOUND" ) )
*/
           )
    	.out( new FileWriter( out ) );   			

    try
    {
      Desktop.getDesktop().open( out );
    }
    catch ( IOException e ) { e.printStackTrace(); }
  }
}
