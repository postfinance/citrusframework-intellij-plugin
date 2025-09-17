package ch.postfinance.citrusframework.plugin;

import static com.intellij.ide.highlighter.XmlFileType.DEFAULT_EXTENSION;
import static com.intellij.ide.highlighter.XmlFileType.DOT_DEFAULT_EXTENSION;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.citrusframework.CitrusSettings;

/**
 * Utility class for working with {@link VirtualFile} instances in the context of Citrus test files.
 * <p>
 * Provides methods to:
 * <ul>
 *   <li>Collect and filter Citrus test files from a set of VirtualFiles</li>
 *   <li>Check whether a given set contains at least one test file</li>
 *   <li>Retrieve formatted test file names</li>
 * </ul>
 */
public final class VirtualFileUtil {

  private VirtualFileUtil() {
    // Prevent instantiation
  }

  /**
   * Whether the given array of {@link VirtualFile} is null or empty.
   *
   * @param virtualFiles array of files or directories to check
   * @return true if the array is null or has no elements, otherwise false
   */
  private static boolean isEmptyArray(VirtualFile[] virtualFiles) {
    return isNull(virtualFiles) || virtualFiles.length == 0;
  }

  /**
   * Retrieves the names of all Citrus test files contained in the given {@link VirtualFile} array.
   * <p>
   * Directories are traversed recursively. Duplicates are avoided by using the file path as a unique key.
   *
   * @param virtualFiles array of files or directories to scan
   * @return a comma-separated list of modified test file names, or an empty string if none are found
   */
  public static String retrieveTestFileNames(VirtualFile[] virtualFiles) {
    if (isEmptyArray(virtualFiles)) {
      return "";
    }

    Map<String, VirtualFile> foundFiles = new TreeMap<>();
    iterateThroughChildren(virtualFiles, file ->
      foundFiles.put(file.getPath(), file)
    );

    return foundFiles
      .values()
      .stream()
      .filter(VirtualFileUtil::isTestFile)
      .map(VirtualFileUtil::formatTestFileName)
      .collect(joining(","));
  }

  /**
   * Checks whether the given array of {@link VirtualFile} contains at least one Citrus test file.
   *
   * @param virtualFiles array of files or directories to scan
   * @return {@code true} if at least one test file is found, otherwise {@code false}
   */
  public static boolean containsAtLeastOneTestFile(VirtualFile[] virtualFiles) {
    if (isEmptyArray(virtualFiles)) {
      return false;
    }

    List<VirtualFile> virtualFileList = new ArrayList<>();
    iterateThroughChildren(virtualFiles, virtualFileList::add);

    return virtualFileList.stream().anyMatch(VirtualFileUtil::isTestFile);
  }

  /**
   * Determines whether a {@link VirtualFile} qualifies as a Citrus test file.
   *
   * @param file the file to check
   * @return {@code true} if the file is a Citrus test XML file, otherwise {@code false}
   */
  private static boolean isTestFile(@Nullable VirtualFile file) {
    if (isNull(file)) {
      return false;
    }

    var defaultExtension = file.getFileType().getDefaultExtension();
    if (!DEFAULT_EXTENSION.equalsIgnoreCase(defaultExtension)) {
      return false;
    }

    return CitrusSettings.getXmlTestFileNamePattern()
      .stream()
      .map(Pattern::compile)
      .anyMatch(pattern -> pattern.matcher(file.getPath()).find());
  }

  /**
   * Returns the test file name in the required format.
   * Example: a file named {@code DebitCards_Test.xml} becomes {@code *DebitCards_Test*}.
   *
   * @param file the file to format
   * @return formatted test file name
   */
  private static @Nonnull String formatTestFileName(@Nonnull VirtualFile file) {
    return "*" + file.getName().replace(DOT_DEFAULT_EXTENSION, "") + "*";
  }

  /**
   * Recursively iterates through the given files and their children, applying the callback to each file.
   *
   * @param virtualFiles array of files or directories to traverse
   * @param callback     callback function to apply to each file
   */
  private static void iterateThroughChildren(
    VirtualFile[] virtualFiles,
    Consumer<VirtualFile> callback
  ) {
    for (VirtualFile file : virtualFiles) {
      if (file.isDirectory()) {
        iterateThroughChildren(VfsUtil.getChildren(file), callback);
      } else {
        callback.accept(file);
      }
    }
  }
}
