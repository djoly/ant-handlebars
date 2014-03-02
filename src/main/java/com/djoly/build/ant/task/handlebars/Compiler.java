package com.djoly.build.ant.task.handlebars;

import org.apache.tools.ant.util.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class Compiler {

    private Context cx;
    private Scriptable scope;
    private Function precompileFunction;
    private CompileOptions options;

    public Compiler(InputStream script,CompileOptions options) throws IOException {
        this.options = options;
        cx = Context.enter();
        scope = cx.initStandardObjects();
        InputStreamReader reader = new InputStreamReader(script);

        cx.evaluateString(scope, FileUtils.readFully(reader),"handlebars.js",1,null);
        NativeObject handlebarsObj = (NativeObject)scope.get("Handlebars", scope);
        precompileFunction = (Function)handlebarsObj.get("precompile");
    }

    public String compile(String template) {
        Object[] args = {template, cx.toObject(new CompileOptions(),scope)};
        return precompileFunction.call(cx,scope,scope,args).toString();
    }

    public void done() {
        Context.exit();
    }
}
