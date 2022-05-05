SQL injection
Login



##Exploit:

------

1.	Hitta någon som har ett befintligt konto på hemsidan (`http://localhost:8080/`).
2.	Använd dennes befintliga användarnamn ex: Brad.
3.	Ta Brad och använd hans användarnamn som: Brad'--
4.	Tryck på loggin då lyckas användaren logga in på Brads konto utan att använda ett riktigt lösenord.




##Vulnerabilty: 

------

Sårbarheten i koden ligger i metoden `login`:

     private static void login(Context context) throws SQLException {
     String username = context.formParam("username");
     String password = context.formParam("password");

    try (Connection c = db.getConnection()) {
        Statement s = c.createStatement();
        ResultSet rows = s.executeQuery(
                "SELECT id FROM user WHERE username = '" + username + "' AND password = " + "'" + password + "'"
        );

        if (rows.next()) {
            context.sessionAttribute("userId", rows.getInt("id"));
            context.sessionAttribute("username", username);
            context.redirect("/");
        }

När hackern använder sig av det stulna användarnamnet och lägger till: -- gör man detta för att 
använda sig av SQL kommentars syntax. Det som matas in efter och hämtas av SQL queryt -- kommer bli bortkommenterat. 
<br>
Detta funkar pga att applikationen använder sig av ren SQL i executeQuery.
Detta betyder att lägger hackern in "Brad-- " kommer SQL queryt bli: SELECT id FROM user WHERE username = 'Brad'; <br>
Vilket resulterar i att SQL kommandot kommer hämta Brads id och sätta det som inloggad och hackern får åtkomst till hela Brads konto. 





##Fix:

------

Vi täpper igen säkerhetshålet genom att använda oss av PreparedStatement:

    String query = "SELECT id FROM user WHERE username =? AND password=?";
    PreparedStatement s = c.prepareStatement(query);
    s.setString(1,username);
    s.setString(2,password);
    ResultSet rows = s.executeQuery();

     if (rows.next()) {
        context.sessionAttribute("userId", rows.getInt("id"));
        context.sessionAttribute("username", username);
        context.redirect("/");

Genom att använda oss av PreparedStatements kan vi begränsa användarinput i inloggningsformuläret. 
PreparedStatements kommer att tolka inputen som värden 






--------
#Anteckningar <br>
Varför fungerade lösningen?
Ska inte gå att bryta sig ur, användarens input tolkas som värden. 
Ersätter '' till "", 