package io.github.karlatemp.bg;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.enumerated.reference.EnumeratedReferenceExtension;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension;
import com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class BlobGenerator {
    public static String genWrapper(String name) {
        StringBuilder sb = new StringBuilder();
        ArrayDeque<String> deque = new ArrayDeque<>();
        Splitter.on('/').split(name).forEach(deque::add);
        String last = deque.removeLast();
        sb.append("<h4 class=\"page-title\">").append(last).append("</h4><ol class=\"breadcrumb\">");
        while (true) {
            String first = deque.poll();
            if (first == null) break;
            sb.append("<li><a href=\"javascript:void(0)\">");
            sb.append(first);
            sb.append("</a></li>");
        }
        sb.append("<li class=\"active\"><a href=\"javascript:void(0)\">");
        sb.append(last);
        sb.append("</a></li>");
        /*<h4 class="page-title">Dashboard1 </h4>
                <ol class="breadcrumb">
                    <li>
                        <a href="#">Dashboard</a>
                    </li>

                    <li class="active">
                        Dashboard 1
                    </li>
                </ol>
                <div class="clearfix"></div>*/

        return sb.append("</ol><div class=\"clearfix\"></div>").toString();
    }

    public static String genSidebar(String name, String prefix, Map<String, List<BlobSidebarGen.SidebarEntry>> sidebar) {
        StringBuilder builder = new StringBuilder();
        String nav = null;
        root:
        for (Map.Entry<String, List<BlobSidebarGen.SidebarEntry>> me : sidebar.entrySet()) {
            for (BlobSidebarGen.SidebarEntry bar : me.getValue()) {
                if (bar.name.equals(name)) {
                    nav = me.getKey();
                    break root;
                }
            }
        }
        for (Map.Entry<String, List<BlobSidebarGen.SidebarEntry>> me : sidebar.entrySet()) {
            builder.append("<li class=\"menu-list");
            if (me.getKey().equals(nav)) {
                builder.append(" nav-active");
            }
            builder.append("\"><a href=\"javascript:void(0)\"><i class=\"icon-layers\"></i><span>");
            builder.append(me.getKey());
            builder.append("</span></a>");
            builder.append("<ul class=\"sub-menu-list\">");
            for (BlobSidebarGen.SidebarEntry bar : me.getValue()) {
                builder.append("<li");
                if (bar.name.equals(name)) {
                    builder.append(" class=\"active\"");
                }
                builder.append("><a href=\"");
                builder.append(prefix);
                builder.append(bar.name);
                builder.append(".html\">");
                Iterator<String> iterator = Splitter.on('/').limit(2).split(bar.name).iterator();
                String last;
                do {
                    last = iterator.next();
                } while (iterator.hasNext());
                builder.append(last);
                builder.append("</a></li>");
            }
            builder.append("</ul></li>");
        }
        return builder.toString();
    }

    public static void main(String[] invoke) throws Throwable {
        Map<String, List<BlobSidebarGen.SidebarEntry>> sidebar = BlobSidebarGen.genSidebar();

        // markdown to image
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        //region
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                AbbreviationExtension.create(),
                AnchorLinkExtension.create(),
                AsideExtension.create(),
                AttributesExtension.create(),
                AutolinkExtension.create(),
                DefinitionExtension.create(),
                EmojiExtension.create(),
                EnumeratedReferenceExtension.create(),
                EscapedCharacterExtension.create(),
                FootnoteExtension.create(),
                GfmIssuesExtension.create(),
                GfmUsersExtension.create(),
                GitLabExtension.create(),
                InsExtension.create(),
                MacroExtension.create(),
                MediaTagsExtension.create(),
                SuperscriptExtension.create(),
                TocExtension.create(),
                TypographicExtension.create(),
                YamlFrontMatterExtension.create()
        ));
        //endregion
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        TemplateEngine engine = new TemplateEngine();

        for (BlobSidebarGen.SidebarEntry entry : sidebar.values()
                .stream()
                .collect(
                        LinkedList<BlobSidebarGen.SidebarEntry>::new,
                        LinkedList::addAll,
                        LinkedList::addAll)
        ) {
            Node document = parser.parse(Joiner.on('\n').join(
                    Files.readLines(entry.file, StandardCharsets.UTF_8)
            ));
            String markdown = renderer.render(document);

            Map<String, Object> variables = new HashMap<>();
            variables.put("content", markdown);
            variables.put("copyright", "Copyright &copy; Karlatemp 2020. All rights reserved.");
            variables.put("title", new ArrayDeque<>(Splitter.on('/').splitToList(entry.name)).getLast());
            variables.put("wrapper", genWrapper(entry.name));
            variables.put("prefix", "/blog/assets");
            variables.put("sidebar", genSidebar(entry.name,
                    "/blog-me/",
                    sidebar));
            Context context = new Context(Locale.ROOT, variables);
            String output = engine.process(
                    new TemplateSpec(
                            Joiner.on('\n').join(
                                    Files.readLines(new File("res/index.html"), StandardCharsets.UTF_8)
                            ),
                            TemplateMode.HTML
                    ), context);
            File build = new File("out-build", entry.name + ".html");
            Files.createParentDirs(build);
            Files.write(output.getBytes(StandardCharsets.UTF_8), build);
        }

    }
}
