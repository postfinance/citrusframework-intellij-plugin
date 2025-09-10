package ch.postfinance.citrusframework.intellij.plugin;

import static com.intellij.ide.highlighter.XmlFileType.DEFAULT_EXTENSION;
import static com.intellij.ide.highlighter.XmlFileType.DOT_DEFAULT_EXTENSION;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Util class for retrieving information from VirtualFiles
 */
public final class VirtualFileUtil {

  private static final String TEST_XML = "Test.xml";

  public static String retrieveTestFileNames(VirtualFile[] virtualFiles) {
    Map<String, VirtualFile> foundFiles = new TreeMap<>(); // Using map to avoid duplicated selections
    iterateTroughChildren(virtualFiles, (VirtualFile virtualFile) ->
      foundFiles.put(virtualFile.getPath(), virtualFile)
    );

    return foundFiles
      .values()
      .stream()
      .filter(VirtualFileUtil::isTestFile)
      .map(VirtualFileUtil::modifyFileName)
      .collect(Collectors.joining(",", "", ""));
  }

  private static String modifyFileName(VirtualFile virtualFile) {
    //*GS2010-26866-03_DebitCards_Actions_Card_Deactivate_Test*
    return "*" + virtualFile.getName().replace(DOT_DEFAULT_EXTENSION, "") + "*";
  }

  private static boolean isTestFile(VirtualFile virtualFile) {
    String defaultExtension = virtualFile.getFileType().getDefaultExtension();
    return (
      defaultExtension.equals(DEFAULT_EXTENSION) &&
      virtualFile.getName().endsWith(TEST_XML)
    );
  }

  public static boolean containsAtLeastOneTestFile(VirtualFile[] virtualFiles) {
    List<VirtualFile> virtualFileList = new ArrayList<>();
    iterateTroughChildren(virtualFiles, virtualFileList::add);
    return virtualFileList.stream().anyMatch(VirtualFileUtil::isTestFile);
  }

  /**
   * Receive an array of virtual files and iterate trough all files on it.
   * If a virtual file represents a "Folder", the function is called recursively otherwise
   * pass the virtual file to the callback
   *
   * @param virtualFiles the array of virtual files
   * @param callback     the callback
   */
  private static void iterateTroughChildren(
    VirtualFile[] virtualFiles,
    Consumer<VirtualFile> callback
  ) {
    for (VirtualFile virtualFile : virtualFiles) {
      if (virtualFile.isDirectory()) {
        iterateTroughChildren(VfsUtil.getChildren(virtualFile), callback);
      } else {
        callback.accept(virtualFile);
      }
    }
  }
}
