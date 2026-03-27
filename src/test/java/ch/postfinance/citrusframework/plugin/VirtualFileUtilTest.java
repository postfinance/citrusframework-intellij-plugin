package ch.postfinance.citrusframework.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VirtualFileUtilTest {

  @Nested
  class ContainsAtLeastOneTestFile {

    @Test
    void returns_false_forEmptyArray() {
      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(new VirtualFile[0])
      ).isFalse();
    }

    @Test
    void returns_true_forSingleTestFile(@Mock VirtualFile virtualFileMock) {
      configureAsXmlTestFile(virtualFileMock, "MyTest.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { virtualFileMock }
        )
      ).isTrue();
    }

    @Test
    void returns_false_forNonTestXmlFile(@Mock VirtualFile virtualFileMock) {
      configureAsXmlFile(virtualFileMock, "Something.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { virtualFileMock }
        )
      ).isFalse();
    }

    @Test
    void returns_false_forWrongCasing(@Mock VirtualFile virtualFileMock) {
      configureAsXmlFile(virtualFileMock, "mytest.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { virtualFileMock }
        )
      ).isFalse();
    }

    @Test
    void returns_false_forNonXmlFileType(
      @Mock VirtualFile virtualFileMock,
      @Mock FileType nonXmlFileTypeMock
    ) {
      when(virtualFileMock.isDirectory()).thenReturn(false);
      when(nonXmlFileTypeMock.getDefaultExtension()).thenReturn("txt");
      when(virtualFileMock.getFileType()).thenReturn(nonXmlFileTypeMock);

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { virtualFileMock }
        )
      ).isFalse();
    }

    @Test
    void returns_true_whenMixedFilesContainOneTestFile(
      @Mock VirtualFile nonTestFileMock,
      @Mock VirtualFile testFileMock
    ) {
      configureAsXmlFile(nonTestFileMock, "Config.xml");
      configureAsXmlTestFile(testFileMock, "MyTest.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { nonTestFileMock, testFileMock }
        )
      ).isTrue();
    }

    @Test
    void returns_true_forTestFileInDirectory(
      @Mock VirtualFile directoryMock,
      @Mock VirtualFile testFileMock
    ) {
      when(directoryMock.isDirectory()).thenReturn(true);
      when(directoryMock.getChildren()).thenReturn(
        new VirtualFile[] { testFileMock }
      );
      configureAsXmlTestFile(testFileMock, "NestedTest.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { directoryMock }
        )
      ).isTrue();
    }

    @Test
    void returns_false_forDirectoryWithNoTestFiles(
      @Mock VirtualFile directoryMock,
      @Mock VirtualFile nonTestFileMock
    ) {
      when(directoryMock.isDirectory()).thenReturn(true);
      when(directoryMock.getChildren()).thenReturn(
        new VirtualFile[] { nonTestFileMock }
      );
      configureAsXmlFile(nonTestFileMock, "Config.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { directoryMock }
        )
      ).isFalse();
    }

    @Test
    void returns_true_forExactSuffixMatch(@Mock VirtualFile virtualFileMock) {
      configureAsXmlTestFile(virtualFileMock, "Test.xml");

      assertThat(
        VirtualFileUtil.containsAtLeastOneTestFile(
          new VirtualFile[] { virtualFileMock }
        )
      ).isTrue();
    }
  }

  @Nested
  class RetrieveTestFileNames {

    @Test
    void returns_empty_forEmptyArray() {
      assertThat(
        VirtualFileUtil.retrieveTestFileNames(new VirtualFile[0])
      ).isEmpty();
    }

    @Test
    void returns_wildcardWrappedName_forSingleTestFile(
      @Mock VirtualFile virtualFileMock
    ) {
      configureAsXmlTestFile(virtualFileMock, "MyTest.xml");
      when(virtualFileMock.getPath()).thenReturn("/project/MyTest.xml");

      assertThat(
        VirtualFileUtil.retrieveTestFileNames(
          new VirtualFile[] { virtualFileMock }
        )
      ).isEqualTo("*MyTest*");
    }

    @Test
    void returns_commaSeparated_forMultipleTestFiles(
      @Mock VirtualFile firstFileMock,
      @Mock VirtualFile secondFileMock
    ) {
      configureAsXmlTestFile(firstFileMock, "AlphaTest.xml");
      when(firstFileMock.getPath()).thenReturn("/project/AlphaTest.xml");
      configureAsXmlTestFile(secondFileMock, "BetaTest.xml");
      when(secondFileMock.getPath()).thenReturn("/project/BetaTest.xml");

      assertThat(
        VirtualFileUtil.retrieveTestFileNames(
          new VirtualFile[] { firstFileMock, secondFileMock }
        )
      ).isEqualTo("*AlphaTest*,*BetaTest*");
    }

    @Test
    void filters_outNonTestFiles(
      @Mock VirtualFile testFileMock,
      @Mock VirtualFile nonTestFileMock
    ) {
      configureAsXmlTestFile(testFileMock, "MyTest.xml");
      when(testFileMock.getPath()).thenReturn("/project/MyTest.xml");
      configureAsXmlFile(nonTestFileMock, "Config.xml");
      when(nonTestFileMock.getPath()).thenReturn("/project/Config.xml");

      assertThat(
        VirtualFileUtil.retrieveTestFileNames(
          new VirtualFile[] { testFileMock, nonTestFileMock }
        )
      ).isEqualTo("*MyTest*");
    }

    @Test
    void resolves_filesFromDirectories(
      @Mock VirtualFile directoryMock,
      @Mock VirtualFile testFileMock
    ) {
      when(directoryMock.isDirectory()).thenReturn(true);
      when(directoryMock.getChildren()).thenReturn(
        new VirtualFile[] { testFileMock }
      );
      configureAsXmlTestFile(testFileMock, "NestedTest.xml");
      when(testFileMock.getPath()).thenReturn("/project/dir/NestedTest.xml");

      assertThat(
        VirtualFileUtil.retrieveTestFileNames(
          new VirtualFile[] { directoryMock }
        )
      ).isEqualTo("*NestedTest*");
    }

    @Test
    void deduplicates_sameFilePath(@Mock VirtualFile virtualFileMock) {
      configureAsXmlTestFile(virtualFileMock, "MyTest.xml");
      when(virtualFileMock.getPath()).thenReturn("/project/MyTest.xml");

      assertThat(
        VirtualFileUtil.retrieveTestFileNames(
          new VirtualFile[] { virtualFileMock, virtualFileMock }
        )
      ).isEqualTo("*MyTest*");
    }
  }

  private static void configureAsXmlTestFile(
    VirtualFile virtualFileMock,
    String name
  ) {
    configureAsFile(virtualFileMock, name, XmlFileType.INSTANCE);
  }

  private static void configureAsXmlFile(
    VirtualFile virtualFileMock,
    String name
  ) {
    configureAsFile(virtualFileMock, name, XmlFileType.INSTANCE);
  }

  private static void configureAsFile(
    VirtualFile virtualFileMock,
    String name,
    FileType fileType
  ) {
    when(virtualFileMock.isDirectory()).thenReturn(false);
    when(virtualFileMock.getName()).thenReturn(name);
    when(virtualFileMock.getFileType()).thenReturn(fileType);
  }
}
