package org.teachingextensions.logo.tests;

import junit.framework.TestCase;
import org.teachingextensions.approvals.lite.Approvals;
import org.teachingextensions.approvals.lite.reporters.UseReporter;
import org.teachingextensions.approvals.lite.reporters.macosx.BeyondCompareReporter;
import org.teachingextensions.approvals.lite.util.StringUtils;
import org.teachingextensions.approvals.lite.util.Tuple;
import org.teachingextensions.approvals.lite.util.io.FileUtils;
import org.teachingextensions.approvals.lite.util.velocity.ContextAware.ContextAwareMap;
import org.teachingextensions.approvals.lite.util.velocity.VelocityParser;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

@UseReporter(BeyondCompareReporter.class)
public class ColorGeneration extends TestCase {
  public static String getOtherColors(HashMap<String, List<Tuple<String, String>>> colors, String key, String color) {
    String out = "";
    for (Entry<String, List<Tuple<String, String>>> entry : colors.entrySet()) {
      if (!entry.getKey().equals(key)) {
        if (containsColor(entry.getValue(), color)) {
          out += entry.getKey() + ", ";
        }
      }
    }
    return out;
  }

  private static boolean containsColor(List<Tuple<String, String>> value, String color) {
    for (Tuple<String, String> tuple : value) {
      if (color.equals(tuple.getFirst())) {
        return true;
      }
    }
    return false;
  }

  public void testGeneration() throws Exception {
    HashMap<String, List<Tuple<String, String>>> loadColors = loadColors();
    Approvals.verify(generateColors(loadColors, "colors.java.template"));
  }

  public void testHtmlDisplay() throws Exception {
    HashMap<String, List<Tuple<String, String>>> loadColors = loadColors();
    Approvals.verifyHtml(generateColors(loadColors, "colors.html"));
  }

  private HashMap<String, List<Tuple<String, String>>> loadColors() {
    HashMap<String, List<Tuple<String, String>>> colors = new HashMap<>();
    String[] split = FileUtils.readFromClassPath(getClass(), "colors.txt").split("\n");
    String currentColor = "";
    for (String line : split) {
      String[] parts = StringUtils.stripWhiteSpace(line).split(" ");
      //System.out.println(line);
      if (parts.length == 1) {
        currentColor = parts[0];
      } else {
        add(colors, currentColor, parts[0], parts[1]);
      }
    }
    return colors;
  }

  private String generateColors(HashMap<String, List<Tuple<String, String>>> colors, String template) {
    ContextAwareMap aware = new ContextAwareMap("colors", colors);
    aware.put("finder", this);
    Object[] keys = colors.keySet().toArray();
    Arrays.sort(keys);
    aware.put("keys", keys);
    return VelocityParser.parseFromClassPath(this.getClass(), template, aware);
  }

  private void add(HashMap<String, List<Tuple<String, String>>> colors, String colorGroup, String name,
                   String hexValue) {
    List<Tuple<String, String>> list = colors.get(colorGroup);
    if (list == null) {
      list = new ArrayList<>();
      colors.put(colorGroup, list);
    }
    list.add(new Tuple<>(name, hexValue));
    Collections.sort(list, new BrightnessComparator());
  }

  public class BrightnessComparator implements Comparator<Tuple<String, String>> {
    @Override
    public int compare(Tuple<String, String> o1, Tuple<String, String> o2) {
      Color c1 = Color.decode(o1.getSecond());
      Color c2 = Color.decode(o2.getSecond());
      Integer b1 = c1.getGreen() + c1.getRed() + c1.getBlue();
      Integer b2 = c2.getGreen() + c2.getRed() + c2.getBlue();
      return b1.compareTo(b2);
    }
  }
}
