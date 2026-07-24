package com.tutego.jrtf.office;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for JUnit 5 test classes/methods that need a real office suite installed to
 * headlessly open (and thereby validate) a generated RTF document. Tests annotated with this are
 * skipped &mdash; not failed &mdash; when no such office suite can be found, so the rest of the
 * build stays green on machines without one installed (e.g. most CI images).
 * <p>
 * Detection and the actual headless invocation only support LibreOffice/OpenOffice's
 * {@code soffice} executable (see {@link OfficeAvailableCondition} for how it is located); there
 * is no supported way to drive Microsoft Word headlessly from the command line without COM
 * automation, which is out of scope here.
 * <p>
 * The executable location can be overridden explicitly if it isn't found automatically, via
 * either the system property {@code jrtf.test.soffice} or the environment variable
 * {@code JRTF_TEST_SOFFICE}, e.g. {@code -Djrtf.test.soffice=/opt/libreoffice/program/soffice}.
 *
 * @see OfficeAvailableCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OfficeAvailableCondition.class)
public @interface OfficeAvailable {
}
