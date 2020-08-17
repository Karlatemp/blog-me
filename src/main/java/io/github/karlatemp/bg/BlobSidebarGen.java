package io.github.karlatemp.bg;

import com.google.common.base.Splitter;
import com.google.common.io.Files;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class BlobSidebarGen {
    public static class SidebarEntry {
        public File file;
        public String name;

        @Override
        public String toString() {
            return "SidebarEntry{" +
                    "name='" + name + '\'' +
                    ", file=" + file +
                    '}';
        }
    }

    public static Map<String, List<SidebarEntry>> genSidebar() {
        Map<String, List<SidebarEntry>> result = new LinkedHashMap<>();
        File dir = new File("source");
        for (File f : dir.listFiles()) {
            List<SidebarEntry> entries = Stream.of(f.listFiles())
                    .filter(File::isFile)
                    .filter(it -> it.getName().endsWith(".md"))
                    .sorted(Comparator.comparingInt(file -> {
                        String name = file.getName();
                        return Integer.parseInt(Splitter.on('.').split(name).iterator().next());
                    }))
                    .map(file -> {
                        SidebarEntry entry = new SidebarEntry();
                        entry.file = file;
                        entry.name = f.getName() + '/' + Files.getNameWithoutExtension(file.getName());
                        if (entry.name.equals("index/0.index")) {
                            entry.name = "index";
                        }
                        return entry;
                    }).collect(Collectors.toList());
            result.put(f.getName(), entries);
        }
        return result;
    }

    public static void main(String[] args) {
        Map<String, List<SidebarEntry>> sidebar = genSidebar();
        sidebar.forEach((k, v) -> {
            System.out.println(k + ":");
            v.forEach(val -> System.out.println("\t" + val));
        });
    }
}
