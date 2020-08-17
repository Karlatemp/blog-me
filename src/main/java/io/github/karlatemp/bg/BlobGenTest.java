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

public class BlobGenTest {
    public static Map<String, String> getHeader(List<String> lines) {
        Iterator<String> iterator = lines.iterator();
        Map<String, String> headers = new LinkedHashMap<>();
        Splitter splitter = Splitter.on(": ").limit(2);
        while (iterator.hasNext()) {
            String next = iterator.next();
            iterator.remove();
            if (next.trim().isEmpty()) {
                break;
            }
            Iterable<String> split = splitter.split(next);
            Iterator<String> splitted = split.iterator();
            headers.put(splitted.next(), splitted.next());
        }
        return headers;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void main(String[] args) throws Throwable {
        File from = new File("source/blob/1.Hello.md");
        List<String> lines = Files.readLines(from, StandardCharsets.UTF_8);
        Map<String, String> headers = getHeader(lines);
        System.out.println(headers);

        // markdown to image
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
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
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();


        Node document = parser.parse(Joiner.on('\n').join(lines));
        String html = renderer.render(document);
        System.out.println();
        System.out.println(html);
        TemplateEngine engine = new TemplateEngine();

        Map<String, Object> variables = new HashMap<>();
        variables.put("content", html);
        variables.put("titleX", "TITLE");
        Context context = new Context(Locale.ROOT, variables);
        String output = engine.process(
                new TemplateSpec(
                        Joiner.on('\n').join(
                                Files.readLines(new File("res/index.html"), StandardCharsets.UTF_8)
                        ),
                        TemplateMode.HTML
                ), context);
        System.out.println(output);
        Files.write(output.getBytes(StandardCharsets.UTF_8), new File("res/hello.html"));
    }
}
