package com.tutego.jrtf;

import org.junit.jupiter.api.Test;

import static com.tutego.jrtf.TestSupport.rtf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RtfListTest {

  @Test void listMustBeRegisteredBeforeUse() {
    RtfList list = RtfList.bulleted();
    assertThatExceptionOfType( RtfException.class )
        .isThrownBy( () -> RtfPara.p( "a" ).list( list, 0 ) );
  }

  @Test void unconfiguredLevelIsRejected() {
    RtfList list = RtfList.bulleted();
    Rtf.rtf().lists( list );
    assertThatIllegalArgumentException().isThrownBy( () -> RtfPara.p( "a" ).list( list, 1 ) );
  }

  @Test void paragraphEmitsLsAndIlvlWithMatchingIndent() {
    RtfList list = RtfList.bulleted();
    Rtf.rtf().lists( list );
    String out = rtf( RtfPara.p( "a" ).list( list, 0 ), true );
    assertThat( out ).contains( "\\li720\\fi-360\\ls1\\ilvl0" );
  }

  @Test void secondRegisteredListGetsNextOverrideIndex() {
    RtfList first = RtfList.bulleted();
    RtfList second = RtfList.numbered( RtfList.NumberFormat.DECIMAL );
    Rtf.rtf().lists( first, second );

    assertThat( rtf( RtfPara.p( "a" ).list( first, 0 ), true ) ).contains( "\\ls1\\ilvl0" );
    assertThat( rtf( RtfPara.p( "b" ).list( second, 0 ), true ) ).contains( "\\ls2\\ilvl0" );
  }

  @Test void registeringSameListTwiceKeepsFirstOverrideIndex() {
    RtfList list = RtfList.bulleted();
    Rtf rtf = Rtf.rtf().lists( list );
    rtf.lists( list );
    assertThat( rtf( RtfPara.p( "a" ).list( list, 0 ), true ) ).contains( "\\ls1\\ilvl0" );
  }

  @Test void documentContainsListtableAndListoverridetableWhenListsRegistered() {
    RtfList bullets = RtfList.bulleted();
    String out = Rtf.rtf().lists( bullets )
                     .section( RtfPara.p( "a" ).list( bullets, 0 ) )
                     .out().toString();
    assertThat( out ).contains( "{\\*\\listtable", "{\\list\\listtemplateid", "\\listsimple1",
                                "\\listid" + listIdOf( bullets ), "{\\*\\listoverridetable",
                                "{\\listoverride\\listid" + listIdOf( bullets ) );
  }

  @Test void documentWithoutListsHasNoListtable() {
    String out = Rtf.rtf().p( "a" ).out().toString();
    assertThat( out ).doesNotContain( "\\listtable" );
  }

  @Test void bulletLevelUsesBulletCharAndEmptyLevelnumbers() {
    RtfList bullets = RtfList.bulleted();
    String out = Rtf.rtf().lists( bullets ).section( RtfPara.p( "a" ).list( bullets, 0 ) ).out().toString();
    assertThat( out ).contains( "{\\leveltext \\'01\\'95;}", "{\\levelnumbers ;}" );
  }

  @Test void decimalLevelUsesPlaceholderAndSuffix() {
    RtfList numbers = RtfList.numbered( RtfList.NumberFormat.DECIMAL );
    String out = Rtf.rtf().lists( numbers ).section( RtfPara.p( "1" ).list( numbers, 0 ) ).out().toString();
    assertThat( out ).contains( "{\\leveltext \\'02\\'00\\'2e;}", "{\\levelnumbers \\'01;}", "\\levelnfc0" );
  }

  @Test void multiLevelListPadsToNineListlevelGroups() {
    RtfList list = RtfList.bulleted().level( 1, RtfList.NumberFormat.DECIMAL );
    String out = Rtf.rtf().lists( list ).section( RtfPara.p( "a" ).list( list, 1 ) ).out().toString();
    assertThat( out ).contains( "\\listsimple0" );
    long listlevelCount = out.split( "\\\\listlevel", -1 ).length - 1;
    assertThat( listlevelCount ).isEqualTo( 9 );
  }

  private static int listIdOf( RtfList list ) {
    return list.listId;
  }
}
