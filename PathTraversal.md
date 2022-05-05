Path Traversal



## Exploit
1. I publish fönstret : ../secrets/passwords.txt
2. Går att skriva över befintliga dokument. 



## Vulnerability 
      private static void publish(Context context) throws IOException {
        
         String filename = context.formParam("filename");
         String text = context.formParam("text");
         Path path = Path.of("stories/" + filename);
         Files.writeString(path, text);

         String title = firstLine(path.toFile());
         String url = "/?story=" + filename;
         String content =
            "<h1>Your short story has been published!</h1>" +
            "<p>Read it here: <a href='" + url + "'>" + title + "</p>" +
            "<p><a href='/'>Return to main page</a></p>";
         String html = template("Short Story Published!", content);
         context.result(html);
     }

Filename sätts in av användaren på hemsidan. 




## Fix 

        String filename = context.formParam("filename");
        String text = context.formParam("text");

        if(filename.contains("../")){
            filename = "";
        }

        // Save the story at the specified path based on the user-provided filename.
        Path path = Path.of("stories/" + filename);
        Files.writeString(path, text);


Begränsa vad för input användaren kan skriva in. 

Finns det vägar runt? 

..././ ... Lösningsförslag? Ligger filen i den mappen? boken/lösningsförlag 

