package com.djoly.build.ant.task.handlebars;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.util.FileUtils;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.FileReader;

public class CompileHandlebarsTaskTest {

    private CompileHandlebarsTask task;
    private File compileFile;

    @Before
    public void setUp() {
        compileFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "compiled-templates.js");

        task = new CompileHandlebarsTask();
        task.setTemplateDir(this.getClass().getResource("/templates").getPath());
        task.setCompileFile(compileFile.getAbsolutePath());
    }

    @Test
    public void inclusion_overrideDefaultInclusionPattern() {

        try {
            task.setIncludePattern("**/*.html");
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertFalse(fileContent.contains("templates['foo']"));
            Assert.assertFalse(fileContent.contains("templates['bar']"));
            Assert.assertFalse(fileContent.contains("templates['nested/foo']"));
            Assert.assertFalse(fileContent.contains("templates['nested/bar']"));
            Assert.assertTrue(fileContent.contains("templates['a']"));
            Assert.assertTrue(fileContent.contains("templates['b']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void inclusion_multiplePatterns() {

        try {
            task.setIncludePattern("**/*.html,nested/*.handlebars");
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertFalse(fileContent.contains("templates['foo']"));
            Assert.assertFalse(fileContent.contains("templates['bar']"));
            Assert.assertTrue(fileContent.contains("templates['nested/foo']"));
            Assert.assertTrue(fileContent.contains("templates['nested/bar']"));
            Assert.assertTrue(fileContent.contains("templates['a']"));
            Assert.assertTrue(fileContent.contains("templates['b']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void exclusion_singleFileExclusion() {

        try {
            task.setExcludePattern("bar.handlebars");
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertTrue(fileContent.contains("templates['foo']"));
            Assert.assertFalse(fileContent.contains("templates['bar']"));
            Assert.assertTrue(fileContent.contains("templates['nested/foo']"));
            Assert.assertTrue(fileContent.contains("templates['nested/bar']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void exclusion_patternExclusion() {

        try {
            task.setExcludePattern("**/*bar.handlebars");
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertTrue(fileContent.contains("templates['foo']"));
            Assert.assertFalse(fileContent.contains("templates['bar']"));
            Assert.assertTrue(fileContent.contains("templates['nested/foo']"));
            Assert.assertFalse(fileContent.contains("templates['nested/bar']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void inclusion_matchesAllAndOnlyDotHandlebarsFilesByDefault() {
        try {
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertTrue(fileContent.contains("templates['foo']"));
            Assert.assertTrue(fileContent.contains("templates['bar']"));
            Assert.assertTrue(fileContent.contains("templates['nested/foo']"));
            Assert.assertTrue(fileContent.contains("templates['nested/bar']"));
            Assert.assertFalse(fileContent.contains("templates['a']"));
            Assert.assertFalse(fileContent.contains("templates['b']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void partials_compileAllToPartials() {
        try {
            task.setPartials(true);
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['foo']"));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['bar']"));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['nested/foo']"));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['nested/bar']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }

    @Test
    public void partials_compileOnlyTemplatesMatchingPartialPatternToPartials() {
        try {
            task.setPartialPattern("nested/**");
            task.execute();
            Assert.assertTrue(compileFile.exists());
            String fileContent = FileUtils.readFully(new FileReader(compileFile));

            Assert.assertTrue(StringUtils.isNotBlank(fileContent));
            Assert.assertTrue(fileContent.contains("templates['foo']"));
            Assert.assertTrue(fileContent.contains("templates['bar']"));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['nested/foo']"));
            Assert.assertTrue(fileContent.contains("Handlebars.partials['nested/bar']"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception");
        }
    }
}
