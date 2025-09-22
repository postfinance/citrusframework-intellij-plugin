package ch.postfinance.citrusframework.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class VirtualFileUtilTest {

  @RunWith(Parameterized.class)
  public static class RetrieveTestFileNames {

    @Parameterized.Parameters
    public static Collection<String> data() {
      return List.of("Test", "test", "IT", "it");
    }

    private final String testFileSuffix;

    public RetrieveTestFileNames(String testFileSuffix) {
      this.testFileSuffix = testFileSuffix;
    }

    @Test
    public void returnsFormattedNames_whenTestFilesPresent() {
      VirtualFile file1 = mockTestFile(
        "Payment_" + testFileSuffix + ".xml",
        "/project/Payment_" + testFileSuffix + ".xml"
      );
      VirtualFile file2 = mockTestFile(
        "Debit_" + testFileSuffix + ".xml",
        "/project/Debit_" + testFileSuffix + ".xml"
      );

      String result = VirtualFileUtil.retrieveTestFileNames(
        new VirtualFile[] { file1, file2 }
      );

      assertThat(result).isEqualTo(
        "*Debit_" + testFileSuffix + "*,*Payment_" + testFileSuffix + "*"
      );
    }

    @Test
    public void returnsEmpty_whenNoTestFiles() {
      VirtualFile file = mockNonTestFile("README.md", "/project/README.md");

      String result = VirtualFileUtil.retrieveTestFileNames(
        new VirtualFile[] { file }
      );

      assertThat(result).isEmpty();
    }

    @Test
    public void returnsEmpty_whenJavaTestFileSelected() {
      VirtualFile file = mockNonTestFile(
        "DebitTest.java",
        "/project/DebitTest.java"
      );

      String result = VirtualFileUtil.retrieveTestFileNames(
        new VirtualFile[] { file }
      );

      assertThat(result).isEmpty();
    }
  }

  public static class ContainsAtLeastOneTestFile {

    @Test
    public void returnsTrue_whenAtLeastOneTestFile() {
      VirtualFile file1 = mockTestFile(
        "Invoice_Test.xml",
        "/project/Invoice_Test.xml"
      );
      VirtualFile file2 = mockNonTestFile("README.md", "/project/README.md");

      boolean result = VirtualFileUtil.containsAtLeastOneTestFile(
        new VirtualFile[] { file1, file2 }
      );

      assertThat(result).isTrue();
    }

    @Test
    public void returnsFalse_whenNoTestFiles() {
      VirtualFile file = mockNonTestFile("Notes.txt", "/project/Notes.txt");

      boolean result = VirtualFileUtil.containsAtLeastOneTestFile(
        new VirtualFile[] { file }
      );

      assertThat(result).isFalse();
    }
  }

  private static VirtualFile mockTestFile(String name, String path) {
    var mockFileType = mock(FileType.class);
    when(mockFileType.getDefaultExtension()).thenReturn("xml");

    var virtualFile = mock(VirtualFile.class);
    when(virtualFile.isDirectory()).thenReturn(false);
    when(virtualFile.getName()).thenReturn(name);
    when(virtualFile.getPath()).thenReturn(path);
    when(virtualFile.getFileType()).thenReturn(mockFileType);
    return virtualFile;
  }

  private static VirtualFile mockNonTestFile(String name, String path) {
    FileType txtType = mock(FileType.class);
    when(txtType.getDefaultExtension()).thenReturn("txt");

    VirtualFile file = mock(VirtualFile.class);
    when(file.isDirectory()).thenReturn(false);
    when(file.getName()).thenReturn(name);
    when(file.getPath()).thenReturn(path);
    when(file.getFileType()).thenReturn(txtType);
    return file;
  }
}
