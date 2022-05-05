Path Traversal



## Exploit
1. Gå till hemsidan (`http://localhost:8080/`)
2. Logga in och välj `publish article`
3. I fönstret för att döpa sitt nya dokument lägg in:  `../secrets/passwords.txt` och
   tryck på publisera.
4. Då har man lyckats att ta dig in i en annan mapp i applikationen och skriva över det 
befintliga dokumentet eller skapat ett nytt om det inte redan finns ett med samma namn. 



## Vulnerability 

Sårbarheten finns i metoden ``publish``:

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

Detta går att göra för att `String filename` sätts in av användaren genom en `context.formParam`.
När man sedan sätter Path så adderas bara användarens input med `"stories/"` där utvecklaren hade tänkt att alla nya
dokument skulle sättas in. <br>
Genom att hackern använder sig av `../` innan den anger filnamnet kan den manipulera filename i Path och ändra riktningen på Path till en helt annan mapp. 
Vilket resulterar i att man både kan lägga nya filer inne i andra mappar än den tänkta (`stories/`) och man kan även skriva över redan befintliga filer.



## Fix 

        String filename = context.formParam("filename");
        String text = context.formParam("text");

        if(filename.contains("../")){
            filename = "";
        }

        // Save the story at the specified path based on the user-provided filename.
        Path path = Path.of("stories/" + filename);
        Files.writeString(path, text);


------

## Anteckningar

Begränsa vad för input användaren kan skriva in. 

Finns det vägar runt? 

..././ ... Lösningsförslag? Ligger filen i den mappen? boken/lösningsförlag 

Filename sätts in av användaren på hemsidan. 