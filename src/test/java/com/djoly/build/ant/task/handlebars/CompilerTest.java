package com.djoly.build.ant.task.handlebars;


import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class CompilerTest {

    @Test
    public void templateSourceIsCompiledIntoJs() {

        try {
            InputStream hbScript = Compiler.class.getResourceAsStream("/handlebars-v1.3.0.js");
            Compiler compiler = new Compiler(hbScript,new CompileOptions());

            String template = "<div class=\"entry\">\n" +
                    "  <h1>{{title}}</h1>\n" +
                    "  <div class=\"body\">\n" +
                    "    {{body}}\n" +
                    "  </div>\n" +
                    "</div>";

            String res = compiler.compile(template);

            assertNotNull(res);
        } catch (IOException e) {
            fail("Failed to instantiate Compiler");
        }
    }
}
