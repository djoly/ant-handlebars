<!DOCTYPE html>
<html>
<head>
    <title>Ant Handlebars: Samples</title>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/1.3.0/handlebars.runtime.min.js"></script>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.js"></script>
    <script type="text/javascript" src="compiled-partials.js"></script>
    <script type="text/javascript" src="compiled-templates.js"></script>

    <script type="text/javascript">
        $(document).ready(function(){

            $('#welcomeWrapper').html(Handlebars.templates['welcome']({
                name: 'Curious George'
            }));

            var friends = [
                { firstName: 'Thomas', lastName: 'Jefferson' },
                { firstName: 'George', lastName: 'Washington' },
                { firstName: 'James', lastName: 'Madison' }
            ];

            $('#friendsWrapper').html(Handlebars.templates['friends-list']({
                friends : friends
            }));
        });
    </script>
</head>
<body>
    <h2>Simple app demonstrating basic usage of compiled handlebar templates.</h2>
    <div id="welcomeWrapper"></div>
    <div id="friendsWrapper"></div>
</body>
</html>