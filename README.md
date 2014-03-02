#ant-handlebars, version 0.0.1

Custom Ant task to precompile handlebar.js templates


This is a custom ant task that precompiles [handlebars.js](http://handlebarsjs.com/) templates.

To build this task, first install [gradle](http://www.gradle.org/). If you want to
run the samples, also install [maven](http://maven.apache.org/).

##Get it!

```
git clone https://github.com/djoly/ant-handlebars.git
cd ant-handlebars
gradle build
```

##Options

###templateDir (required)
This is your root template directory.

###compileFile (required)
This the target compile file. Your compiled templates will be dumped to this file.

###partialPattern (optional)
Any templates matching the specified pattern will be compiled to the Handlebars.partials namespace. All others will be compiled to the Handlebars.templates namespace.

###partials (optional)
Specifies if the compiled templates should be compiled as partials. If this flag is set, all templates will be dumped to the Handlebars.partials namespace. If this option is set, partialsPattern is ignored.

###includePattern (optional)
Only templates matching the specified pattern will be compiled. Default pattern is
```
**/*.handlebars
```

###excludePattern (optional)
Exclude matching templates from being compiled.

###handlebarsFile (optional)
Handlebars version 1.3.0 is included with this task. If you need to use a different version specify the file here.

NOTE: This feature is currently untested.

##Running the samples

###Gradle example
```
#clone project
git clone https://github.com/djoly/ant-handlebars.git

#navigate to project root
cd ant-handlebars

#publish jar to local maven .m2 repo
gradle publishToMavenLocal

#navigate to gradle sample project
cd samples/gradle

#run launch web application
gradle jettyRun

```

Open the [web app](http://localhost:8090/gradle) in your browser.

##TO-DO

Add AMD support, compile options, and more samples.

