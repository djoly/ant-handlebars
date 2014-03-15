<!DOCTYPE html>
<html>
<head>
    <title>Ant Handlebars: Samples</title>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/require.js/2.1.10/require.min.js"></script>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/1.3.0/handlebars.runtime.amd.min.js"></script>
    <script type="text/javascript">

        requirejs.config({
            paths: {
                "templates": "compiled-templates",
                "partials": "compiled-partials",
                "jquery": "//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery"
            }
        });

        define('hbs',['handlebars.runtime','templates','partials'],function(handlebars,templates,partials){
            return handlebars.default;
        });

        require(['hbs','jquery'],function(hbs,$){
            $('#welcomeWrapper').html(hbs.templates['welcome']({
                name: 'Curious George'
            }));

            var friends = [
                { firstName: 'Thomas', lastName: 'Jefferson' },
                { firstName: 'George', lastName: 'Washington' },
                { firstName: 'James', lastName: 'Madison' }
            ];

            $('#friendsWrapper').html(hbs.templates['friends-list']({
                friends : friends
            }));

        });

    </script>
</head>
<body>
    <h2>Simple app demonstrating basic usage of compiled handlebar templates with AMD.</h2>
    <div id="welcomeWrapper"></div>
    <div id="friendsWrapper"></div>
</body>
</html>