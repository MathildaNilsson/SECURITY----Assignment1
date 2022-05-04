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

Sårbarheten i koden ligger i metoden login:

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