public class SqlInjection {
    // Vulnerability

/*    try (Connection c = db.getConnection()) {
        Statement s = c.createStatement();
        ResultSet rows = s.executeQuery(
                "SELECT id FROM user WHERE username = '" + username + "' AND password = " + "'" + password + "'"
        );

        if (rows.next()) {
            context.sessionAttribute("userId", rows.getInt("id"));
            context.sessionAttribute("username", username);
            context.redirect("/");
        }*/

    // Fix

/*    String query = "SELECT id FROM user WHERE username =? AND password=?";
    PreparedStatement s = c.prepareStatement(query);
    s.setString(1,username);
    s.setString(2,password);
    ResultSet rows = s.executeQuery();

     if (rows.next()) {
        context.sessionAttribute("userId", rows.getInt("id"));
        context.sessionAttribute("username", username);
        context.redirect("/");*/



}
