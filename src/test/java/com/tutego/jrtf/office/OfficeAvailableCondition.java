package com.tutego.jrtf.office;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Backing {@link ExecutionCondition} for {@link OfficeAvailable}: skips the test unless a
 * LibreOffice/OpenOffice {@code soffice} executable can be located on this machine.
 */
final class OfficeAvailableCondition implements ExecutionCondition {

  static final String SOFFICE_PATH_PROPERTY = "jrtf.test.soffice";
  static final String SOFFICE_PATH_ENV = "JRTF_TEST_SOFFICE";

  private static final List<String> COMMON_INSTALL_PATHS = Collections.unmodifiableList( Arrays.asList(
      "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
      "C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe",
      "C:\\Program Files\\OpenOffice\\program\\soffice.exe",
      "/usr/bin/soffice",
      "/usr/lib/libreoffice/program/soffice",
      "/opt/libreoffice/program/soffice",
      "/Applications/LibreOffice.app/Contents/MacOS/soffice"
  ) );

  private static final List<String> EXECUTABLE_NAMES =
      Collections.unmodifiableList( Arrays.asList( "soffice", "soffice.exe", "soffice.com" ) );

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition( ExtensionContext context ) {
    Path soffice = findSoffice();

    if ( soffice == null )
      return ConditionEvaluationResult.disabled(
          "No LibreOffice/OpenOffice \"soffice\" executable found. Install LibreOffice, or point "
        + "at one explicitly with -D" + SOFFICE_PATH_PROPERTY + "=<path> or the " + SOFFICE_PATH_ENV
        + " environment variable." );

    return ConditionEvaluationResult.enabled( "Found soffice at " + soffice );
  }

  /**
   * Locates a {@code soffice} executable, checked in this order: the {@code jrtf.test.soffice}
   * system property, the {@code JRTF_TEST_SOFFICE} environment variable, a handful of common
   * installation directories, and finally {@code PATH}.
   *
   * @return Path to the executable, or {@code null} if none was found.
   */
  static Path findSoffice() {
    String override = System.getProperty( SOFFICE_PATH_PROPERTY );
    if ( override == null || override.trim().isEmpty() )
      override = System.getenv( SOFFICE_PATH_ENV );

    if ( override != null && !override.trim().isEmpty() ) {
      Path path = Paths.get( override );
      return Files.isExecutable( path ) ? path : null;
    }

    for ( String candidate : COMMON_INSTALL_PATHS ) {
      Path path = Paths.get( candidate );
      if ( Files.isExecutable( path ) )
        return path;
    }

    return findOnPath();
  }

  private static Path findOnPath() {
    String path = System.getenv( "PATH" );
    if ( path == null )
      return null;

    for ( String dir : path.split( File.pathSeparator ) ) {
      for ( String exe : EXECUTABLE_NAMES ) {
        Path candidate = Paths.get( dir, exe );
        if ( Files.isExecutable( candidate ) )
          return candidate;
      }
    }

    return null;
  }
}
