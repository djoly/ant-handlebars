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
    private String includePattern;

    /**
     * Pattern of templates that should be ignored.
     */
    private String excludePattern;

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

    private static final String COMPILE_FILE_START =
            "(function() {" +
            "var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};";

    private static final String COMPILE_FILE_END = "})();";
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

        CompileOptions options = new CompileOptions();

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            Compiler compiler = new Compiler(getHandlebarsFile(),options);

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

            bw.write(COMPILE_FILE_START);
            bw.newLine();

            while(filesIterator.hasNext()) {
                Resource fr = filesIterator.next();

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

            bw.write(COMPILE_FILE_END);
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
        return includePattern != null ? includePattern : "**/*.handlebars";
    }

    public void setIncludePattern(String includePattern) {
        this.includePattern = includePattern;
    }

    public String getExcludePattern() {
        return excludePattern != null ? excludePattern : StringUtils.EMPTY;
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
        return handlebarsFile != null ? handlebarsFile :
                this.getClass().getResource("/handlebars-v1.3.0.js").getPath();
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
}
