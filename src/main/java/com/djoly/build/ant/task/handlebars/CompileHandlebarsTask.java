package com.djoly.build.ant.task.handlebars;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResourceIterator;
import org.apache.tools.ant.util.FileUtils;

import java.io.*;

public class CompileHandlebarsTask extends Task {

    /**
     * Path to the default handlebars file. If no file is specified,
     * the one included with this distribution will be used.
     */
    private String handlebarsFile;

    /**
     * Pattern with which to match templates in the base templateDir or its
     * subdirectories. Defaults to ** /*.handlebars
     */
    private String includePattern = "**/*.handlebars";

    /**
     * Pattern of templates that should be ignored.
     */
    private String excludePattern = StringUtils.EMPTY;

    /**
     * Whether or not the matching templates should be compiled as partials.
     * If this is set to true, all matched templates will be compiled as partials.
     */
    private boolean partials;

    /**
     * If set, and partials is false, all files matching this pattern
     * are compiled as partials. If partials is true, setting
     * partialPattern has no effect.
     */
    private String partialPattern;

    /**
     * The file that the compiled templates will be written to.
     */
    private String compileFile;

    /**
     * Root location of handlebar templates
     */
    private String templateDir;

    /**
     * Specifies the templates be compiled as an AMD module.
     */
    private boolean amd;

    /**
     * Path to handlebars. Relevant for AMD only.
     */
    private String handlebarsPath = StringUtils.EMPTY;

    /**
     * Template namespace.
     */
    private String namespace = "Handlebars.templates";

    private static final String COMPILE_FILE_VAR_LINE = "var template = Handlebars.template, templates = %s = %s || {};";
    private static final String COMPILE_FILE_START = "(function() {\n";
    private static final String COMPILE_FILE_END = "})();";
    private static final String AMD_COMPILE_FILE_START =
            "define(['%shandlebars.runtime'], function(Handlebars) {\nHandlebars = Handlebars['default'];\n";
    private static final String AMD_COMPILE_FILE_END_PARTIALS = "return Handlebars.partials;});";
    private static final String AMD_COMPILE_FILE_END_TEMPLATES = "return templates;});";
    private static final String TEMPLATE_ASSIGNMENT = "templates['%s'] = template(%s);";
    private static final String PARTIAL_ASSIGNMENT = "Handlebars.partials['%s'] = template(%s);";

    public void execute() throws BuildException {

        if(templateDir == null) {
            throw new BuildException("templateDir not specified. Please specify the root directory where your templates are located.");
        }

        File baseDir = new File(templateDir);
        if(!baseDir.isDirectory()) {
            throw new BuildException("Specified templateDir path " + baseDir.getAbsolutePath() + " is not a directory");
        }

        if(!baseDir.canRead()) {
            throw new BuildException("Cannot read template directory " + baseDir.getAbsolutePath());
        }

        if(compileFile == null) {
            throw new BuildException("compileFile not specified. Please specify the path to the compiled JS file.");
        }

        File file = new File(compileFile);

        if(file.isDirectory()) {
            throw new BuildException("Specified compileFile is a directory!");
        }

        if(file.exists()) {
            log("Will replace existing compiled file " + file.getAbsolutePath());
        }

        if(amd && !partials && partialPattern != null) {
            throw new BuildException("Cannot compile templates and partials to same file when in AMD mode.");
        }

        CompileOptions options = new CompileOptions();

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            Compiler compiler = new Compiler(loadHandleBarsScript(),options);

            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            log("Scanning " + templateDir + " for templates...");

            String[] partials = {};
            if(!isPartials() && getPartialPattern() != null) {
                 partials = findFiles(baseDir,getPartialPattern().split(","));
            }

            String[] templates = findFiles(baseDir,getIncludePattern().split(","),getExcludePattern().split(","));

            @SuppressWarnings("deprecation")
            FileResourceIterator filesIterator = new FileResourceIterator(baseDir);
            filesIterator.addFiles(templates);

            bw.write(isAmd() ? String.format(AMD_COMPILE_FILE_START,handlebarsPath) : COMPILE_FILE_START);
            bw.write(String.format(COMPILE_FILE_VAR_LINE,namespace,namespace));
            bw.newLine();

            while(filesIterator.hasNext()) {
                Resource fr = filesIterator.nextResource();

                InputStreamReader r = new InputStreamReader(fr.getInputStream());
                String templateContent = FileUtils.readFully(r);
                String compiledContent = compiler.compile(templateContent);

                String name = fr.getName().substring(0,fr.getName().lastIndexOf("."));

                if(isPartials() ||
                    (getPartialPattern() != null && ArrayUtils.contains(partials,fr.getName()))) {
                    bw.write(String.format(PARTIAL_ASSIGNMENT,name,compiledContent));
                } else {
                    bw.write(String.format(TEMPLATE_ASSIGNMENT,name,compiledContent));
                }
                bw.newLine();
            }

            bw.write(isAmd() ? (isPartials() ? AMD_COMPILE_FILE_END_PARTIALS : AMD_COMPILE_FILE_END_TEMPLATES) : COMPILE_FILE_END);
            bw.flush();
            compiler.done();

            log(String.format("Compiled %s templates to %s", templates.length, file.getAbsolutePath()));

        } catch (IOException ioe) {
            throw new BuildException("Failed to compile. Reason [ " + ioe.getMessage() +"]");
        } finally {
            if(fw != null) {
                try {
                    fw.close();

                    if(bw != null) {
                        bw.close();
                    }
                } catch (IOException ioe2) {
                    log(ioe2, Project.MSG_ERR);
                }
            }
        }

    }

    private String[] findFiles(File baseDir, String[] includes) {
        return findFiles(baseDir, includes, null);
    }

    private String[] findFiles(File baseDir, String[] includes, String[] excludes) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(baseDir);
        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.scan();
        return ds.getIncludedFiles();
    }

    public String getIncludePattern() {
        return includePattern;
    }

    public void setIncludePattern(String includePattern) {
        this.includePattern = includePattern;
    }

    public String getExcludePattern() {
        return excludePattern;
    }

    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern;
    }

    public boolean isPartials() {
        return partials;
    }

    public void setPartials(boolean partials) {
        this.partials = partials;
    }

    public String getPartialPattern() {
        return partialPattern;
    }

    public void setPartialPattern(String partialPattern) {
        this.partialPattern = partialPattern;
    }

    public String getCompileFile() {
        return compileFile;
    }

    public void setCompileFile(String compileFile) {
        this.compileFile = compileFile;
    }

    public String getHandlebarsFile() {
        return handlebarsFile;
    }

    public void setHandlebarsFile(String handlebarsFile) {
        this.handlebarsFile = handlebarsFile;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public boolean isAmd() {
        return amd;
    }

    public void setAmd(boolean amd) {
        this.amd = amd;
    }

    public void setHandlebarsPath(String handlebarsPath) {
        this.handlebarsPath = handlebarsPath;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private InputStream loadHandleBarsScript() {
        InputStream scriptStream = null;
        if(handlebarsFile == null) {
            scriptStream = this.getClass().getResourceAsStream("/handlebars-v1.3.0.js");
        } else {
            try {
                scriptStream = new FileInputStream(handlebarsFile);
            } catch(FileNotFoundException e) {
                throw new BuildException("Could not load handlebars script file " + handlebarsFile);
            }
        }
        return scriptStream;
    }
}
