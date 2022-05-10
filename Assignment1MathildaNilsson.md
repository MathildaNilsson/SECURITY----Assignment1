Path Traversal <br>
Publish article <br>
Mathilda Nilsson



## Exploit
1. Gå till hemsidan (`http://localhost:8080/`)
2. Logga in en användare och välj `publish a short story` för att skapa en ny artikel.
3. I fältet för att döpa sitt nya dokument lägg in:  `../secrets/passwords.txt` och
   tryck på publisera.
4. Då har man lyckats att ta sig in i en annan mapp i applikationen och lägga till ett nytt dokument eller skriva över ett befintligt om det redan finns ett med samma namn.



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

Vi vill kunna kontrollera vad användaren har skrivit in i `formParam("filename")` fältet
och gör detta genom att skapa två Path objekt som vi kan använda som referenser för användar input och ursprungsmapp.  <br>
Vi skapar en Path av den input vi fått av användaren i `formParam`, där vi sätter `"stories/"` + `filename` och
använder oss av ``toAbsolutPath`` vilket kommer returnera en Path som representerar den absoluta pathen som
ännu inte finns. Sedan använder vi oss av ``normalize`` för att returnera ett resultat av en Path som tar bort alla oväntade värden så som: `../`, `./` etc. <br>
<br>
Sedan skapar vi en ny Path av mappen(``"Stories/"``) som vi kommer vilja referera till som den riktiga mappen så vi gör detta genom att skapa en motsvarighet till den som ``folder``, där vi använder
vi oss av ``toRealPath`` för att uppnå detta. 
<br><br>
Genom att kontrollera att vårt nya dokument `path` som användaren namngett och som vi normaliserat motsvarar den Path vi skapat av referensen av vår `folder` kan vi säkerställa att
användaren inte kommer runt säkerheten i applikationen och får istället ett felmeddelande som säger
att det inte är tillåtet att komma åt andra mappar än den utvecklaren satt upp som default.


<br><br>

------

<br><br>

SQL injection <br>
Login <br>
Mathilda Nilsson


## Exploit:


1.	Hitta någon som har ett befintligt konto på hemsidan (`http://localhost:8080/`).
2.	Använd dennes befintliga användarnamn ex: Brad
3.	Ta Brad och använd hans användarnamn som: `Brad'--`
4.	Tryck på loggin då lyckas användaren logga in på Brads konto utan att använda ett riktigt lösenord.


## Vulnerabilty:


Sårbarheten i koden ligger i metoden `login`:

     private static void login(Context context) throws SQLException {
     String username = context.formParam("username");
     String password = context.formParam("password");

    try (Connection c = db.getConnection()) {
        Statement s = c.createStatement();
        ResultSet rows = s.executeQuery(
                "SELECT id FROM user WHERE username = '" + username + "' AND password = " + "'" + password + "'"
        );


När hackern använder sig av det stulna användarnamnet och lägger till: `--` gör man detta för att
använda sig av SQL kommentars syntax. Det som matas in efter och hämtas av SQL queryt -- kommer bli bortkommenterat.
<br>
Detta funkar pga att applikationen använder sig av ren SQL i executeQuery.
Username sätts på första raden i metoden `String username = context.formParam("username");` där användarens inmatning tas
in och styr hela SQL raden i `ResultSet rows = s.executeQuery(SELECT id FROM user WHERE username = '")`.
<br><br>
Detta betyder att lägger hackern in `Brad--`  kommer SQL queryt ändras till: <br> `SELECT id FROM user WHERE username = 'Brad';` <br>
Vilket resulterar i att SQL kommandot kommer hämta Brads id och sätta det som inloggad och hackern får åtkomst till hela Brads konto.



## Fix:

Vi täpper igen säkerhetshålet genom att använda oss av PreparedStatement:

    String query = "SELECT id FROM user WHERE username =? AND password=?";
    PreparedStatement s = c.prepareStatement(query);
    s.setString(1,username);
    s.setString(2,password);
    ResultSet rows = s.executeQuery();


Genom att använda oss av PreparedStatements kan vi begränsa användarinput i inloggningsformuläret.
PreparedStatements kommer att tolka inputen som värden och inte som ett rent SQL query där `'` används som fritext och
som en hacker kan utnyttja och sätta in eget `'` för att ändra queryt. <br><br>
Utan det som PreparedStatement hjälper oss att göra är vi specificerar vart i queryt vi vill ha användarens input med ``?``.<br>
Vi kan sedan med hjälp av PreparedStatement specificera vad vi vill ha för värde på varje ``?`` med `SetString` och applikationen kommer då att endast tolka
varje värde som en satt sträng. 
