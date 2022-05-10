Path Traversal <br>
Publish article <br>
Mathilda Nilsson



## Exploit
1. Gå till hemsidan (`http://localhost:8080/`)
2. Logga in en användare och välj `publish a short story` för att skapa en ny artikel.
3. I fältet för att döpa sitt nya dokument lägg in:  `../secrets/passwords.txt` och
   tryck på publisera.
4. Då har man lyckats att ta sig in i en annan mapp i applikationen och lägga till ett nytt dokument där eller skriva över ett befintligt om det redan finns ett med samma namn.



## Vulnerability 

Sårbarheten finns i metoden ``publish``:

      private static void publish(Context context) throws IOException {
        
         String filename = context.formParam("filename");
         String text = context.formParam("text");
         Path path = Path.of("stories/" + filename);
         Files.writeString(path, text);

         String title = firstLine(path.toFile());
         String url = "/?story=" + filename;


Detta går att göra för att `String filename` sätts in av användaren genom en `context.formParam`.
När man sedan sätter Path så adderas bara användarens input med `"stories/"` där utvecklaren hade tänkt att alla nya
dokument skulle sättas in. På detta sätt ger man en poteniell hacker full makt till att skriva in precis vad den vill i fältet som
en sträng.<br><br>
Genom att hackern använder sig av `../` innan den anger filnamnet kan den manipulera filename och ändra riktningen på Path till en helt annan mapp. 
Vilket resulterar i att man både kan lägga till nya filer inne i andra mappar än den tänkta (`stories/`) och man kan även skriva över redan befintliga filer
om man döper det nya dokumentet till samma namn som ett redan befintligt dokument. 



## Fix

Vi lägger till följande kod i metoden `publish`:

    private static void publish(Context context) throws IOException {
        String filename = context.formParam("filename");
        String text = context.formParam("text");

        Path path = Path.of("stories/" + filename).toAbsolutePath().normalize();
        Path folder = Path.of("stories").toRealPath();

        if(!path.startsWith(folder)){
            context.result("Not allowed to save files outside the story folder.");
            return;
        }

Vi vill begränsa vad användaren kan skriva in i `formParam("filename")` fältet 
och gör detta genom att skapa Path objekt som vi kan referera till. <br>
Vi skapar en Path av den input vi fått av användaren i `formParam`, där vi sätter `"stories/"` + `filename` och 
använder oss av ``toAbsolutPath`` vilket kommer returnera en Path som representerar den absoluta pathen som 
ännu inte finns. Sedan använder vi oss av ``normalize`` för att returnera ett resultat av en Path som tar bort alla oväntade värden så som: `../`, `./` etc. <br>
<br>
Sedan skapar vi en ny Path av den folder (``"Stories/"``) vi kommer vilja referera till så vi gör detta genom att skapa en motsvarighet till ursprungs mappen. Därför använder
vi oss av ``toRealPath`` . 
<br><br>
Genom att kontrollera att vårt nya dokument `path` som användaren namngett och som vi normaliserat motsvarar den Path vi skapat av referensen av vår `folder` kan vi säkerställa att 
användaren inte kommer runt säkerheten i applikationen och får istället ett felmeddelande som säger
att det inte är tillåtet att komma åt andra mappar än den utvecklaren satt upp som default.


