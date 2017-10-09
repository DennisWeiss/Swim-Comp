import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DSV6Analyzer {
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
    Statement statement = connection.createStatement();

    String veranstaltungBezeichnung = "";
    String veranstaltungOrt = "";
    String bahnlange = "";
    String zeitmessung = "";
    String veranstalterName = "";

    public DSV6Analyzer() throws SQLException {
    }

    public void analyzeFile(File file) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";

        try {

            statement.execute("CREATE TABLE Veranstaltung(" +
                    "VID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "Bezeichnung VARCHAR(200) NOT NULL, " +
                    "Ort VARCHAR(200) NOT NULL, " +
                    "Bahnlange VARCHAR(40) NOT NULL, " +
                    "VerID INTEGER REFERENCES Veranstalter, " +
                    "Zeitmessung VARCHAR(40) NOT NULL);"
            );

            statement.execute("CREATE TABLE Veranstalter(" +
                    "VerID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "Name VARCHAR(100) UNIQUE NOT NULL);"
            );

            statement.execute("CREATE TABLE Ausrichter(" +
                    "AID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "NameAusrichter VARCHAR(200) NOT NULL, " +
                    "NameAnsprechpartner VARCHAR(200) NOT NULL, " +
                    "Strasse VARCHAR(200), " +
                    "PLZ VARCHAR(20), " +
                    "Ort VARCHAR(60), " +
                    "Land VARCHAR(100), " +
                    "Telefon VARCHAR(20), " +
                    "Fax VARCHAR(20), " +
                    "eMail VARCHAR(60) NOT NULL);"
            );

            statement.execute("CREATE TABLE Abschnitt(" +
                    "Abschnittsnummer INTEGER PRIMARY KEY, " +
                    "Anfangszeit TIMESTAMP NOT NULL, " +
                    "Angabe CHAR(1));"
            );

            statement.execute("CREATE TABLE Kampfgericht(" +
                    "Abschnittsnummer INTEGER REFERENCES Abschnitt, " +
                    "Position VARCHAR(3) NOT NULL, " +
                    "KID INTEGER REFERENCES Kampfrichter, " +
                    "PRIMARY KEY(Abschnittsnummer, Position, KID));"
            );

            statement.execute("CREATE TABLE Kampfrichter(" +
                    "KID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "Nachname VARCHAR(60) NOT NULL, " +
                    "Vorname VARCHAR(60) NOT NULL, " +
                    "VID INTEGER REFERENCES Verein);"
            );

            statement.execute("CREATE TABLE Wettkampf(" +
                    "Wettkampfnummer INTEGER PRIMARY KEY, " +
                    "Wettkampfart CHAR(1) NOT NULL, " +
                    "Abschnittsnummer INTEGER NOT NULL, " +
                    "Starter INTEGER, " +
                    "Einzelstrecke INTEGER NOT NULL, " +
                    "Technik CHAR(1) NOT NULL, " +
                    "Ausubung VARCHAR(2) NOT NULL, " +
                    "Geschlecht CHAR(1) NOT NULL, " +
                    "Bestenliste CHAR(2) NOT NULL, " +
                    "Qulaifikationswettkampfnummer INTEGER, " +
                    "Qualifikationswettkampfart CHAR(1));"
            );

            statement.execute("CREATE TABLE Wertung(" +
                    "WertungsID INTEGER PRIMARY KEY, " +
                    "Wettkampfnummer INTEGER REFERENCES Wettkampf, " +
                    "Wertungstyp CHAR(2) NOT NULL, " +
                    "JahrgangVon INTEGER NOT NULL, " +
                    "JahrgangBis INTEGER, " +
                    "Wertungsname VARCHAR(60) NOT NULL);"
            );

            statement.execute("CREATE TABLE Verein(" +
                    "VID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "Bezeichnung VARCHAR(100) UNIQUE NOT NULL, " +
                    "Kennzahl INTEGER, " +
                    "Landesschwimmverband INTEGER DEFAULT NULL, " +
                    "FINA INTEGER DEFAULT NULL);"
            );

            statement.execute("CREATE TABLE Ergebnis(" +
                    "EID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "WertungsID INTEGER REFERENCES Wertung, " +
                    "Platz INTEGER NOT NULL, " +
                    "GrundDerNichtwertung VARCHAR(100), " +
                    "SID INTEGER REFERENCES Schwimmer, " +
                    "VeranstaltungsID INTEGER REFERENCES VeranstaltungSchwimmer, " +
                    "Zeit TIMESTAMP NOT NULL, " +
                    "Disqualifikationsbemerkung VARCHAR(200), " +
                    "ErhohtesNachtraglichesMeldegeld CHAR(1));"
            );

            statement.execute("CREATE TABLE VeranstaltungSchwimmer(" +
                    "VeranstaltungsID INTEGER PRIMARY KEY, " +
                    "VID INTEGER REFERENCES Veranstaltung, " +
                    "SID INTEGER REFERENCES Schwimmer);"
            );

            statement.execute("CREATE TABLE Zwischenzeit(" +
                    "ZID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "VeranstaltungsID INTEGER REFERENCES VeranstaltungSchwimmer, " +
                    "Wettkampfnummer INTEGER REFERENCES Wettkampf, " +
                    "Distanz INTEGER NOT NULL, " +
                    "Zeit TIMESTAMP NOT NULL);"
            );

            statement.execute("CREATE TABLE Reaktion(" +
                    "RID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "VeranstaltungsID INTEGER REFERENCES VeranstaltungSchwimmer, " +
                    "Wettkampfnummer INTEGER REFERENCES Wettkampf, " +
                    "Art CHAR(1), " +
                    "Zeit TIMESTAMP);"
            );

            statement.execute("CREATE TABLE Schwimmer(" +
                    "SID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "Nachname VARCHAR(60) NOT NULL, " +
                    "Vorname VARCHAR(60) NOT NULL, " +
                    "DSVID INTEGER NOT NULL, " +
                    "Geschlecht CHAR(1) NOT NULL, " +
                    "Jahrgang INTEGER NOT NULL, " +
                    "VID INTEGER REFERENCES Verein);"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }

        do {
            try {
                line = reader.readLine();
                analyzeLine(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (line != null);
    }

    private void analyzeLine(String line) {
        try {
            if (line.substring(0, 14).equals("VERANSTALTUNG:")) {
                veranstaltungBezeichnung = stringsSeparatedBySemicolons(line, 15).get(0);
                veranstaltungOrt = stringsSeparatedBySemicolons(line, 15).get(1);
                bahnlange = stringsSeparatedBySemicolons(line, 15).get(2);
                zeitmessung = stringsSeparatedBySemicolons(line, 15).get(3);
            } else if (line.substring(0, 13).equals("VERANSTALTER:")) {
                veranstalterName = stringsSeparatedBySemicolons(line, 14).get(0);
                statement.execute("INSERT INTO Veranstalter(Name)" +
                        "VALUES ('" + veranstalterName + "');"
                );
                statement.execute("INSERT INTO Veranstaltung(Bezeichnung, Ort, Bahnlange, VerID, Zeitmessung) " +
                        "VALUES ('" + veranstaltungBezeichnung + "', '" + veranstaltungOrt + "', '" + bahnlange + "', (SELECT VerID FROM Veranstalter WHERE Name = '" + veranstalterName + "'), '" + zeitmessung + "');"
                );
            } else if (line.substring(0, 11).equals("AUSRICHTER:")) {
                String ausrichterName = stringsSeparatedBySemicolons(line, 12).get(0);
                String ansprechpartner = stringsSeparatedBySemicolons(line, 12).get(1);
                String strasse = stringsSeparatedBySemicolons(line, 12).get(2);
                String plz = stringsSeparatedBySemicolons(line, 12).get(3);
                String ort = stringsSeparatedBySemicolons(line, 12).get(4);
                String land = stringsSeparatedBySemicolons(line, 12).get(5);
                String telefon = stringsSeparatedBySemicolons(line, 12).get(6);
                String fax = stringsSeparatedBySemicolons(line, 12).get(7);
                String email = stringsSeparatedBySemicolons(line, 12).get(8);
                statement.execute("INSERT INTO Ausrichter(NameAusrichter, NameAnsprechpartner, Strasse, PLZ, Ort, Land, Telefon, Fax, eMail) " +
                        "VALUES ('" + ausrichterName +
                        "', '" + ansprechpartner +
                        "', '" + strasse +
                        "', '" + plz +
                        "', '" + ort +
                        "', '" + land +
                        "', '" + telefon +
                        "', '" + fax +
                        "', '" + email + "');"
                );
            } else if (line.substring(0, 10).equals("ABSCHNITT:")) {
                int abschnitt = Integer.parseInt(stringsSeparatedBySemicolons(line, 11).get(0));
                SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy");
                Date date1 = new Date();
                try {
                    date1 = format1.parse(stringsSeparatedBySemicolons(line, 11).get(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                char angabe = 'N';
                if (stringsSeparatedBySemicolons(line, 11).get(3).length() > 0) {
                    angabe = stringsSeparatedBySemicolons(line, 11).get(3).charAt(0);
                }
                Calendar calendar1 = new GregorianCalendar();
                calendar1.setTime(date1);
                System.out.println("INSERT INTO Abschnitt(Abschnittsnummer, Anfangszeit, Angabe) VALUES (" +
                        abschnitt +
                        ", '" + calendar1.get(Calendar.YEAR) + "-" + (calendar1.get(Calendar.MONTH)+1) + "-" + calendar1.get(Calendar.DAY_OF_MONTH) +
                        " " + stringsSeparatedBySemicolons(line, 11).get(2) + ":00" +
                        "', '" + angabe + "');");

                statement.execute("INSERT INTO Abschnitt(Abschnittsnummer, Anfangszeit, Angabe) VALUES (" +
                        abschnitt +
                        ", '" + calendar1.get(Calendar.YEAR) + "-" + (calendar1.get(Calendar.MONTH)+1) + "-" + calendar1.get(Calendar.DAY_OF_MONTH) +
                        " " + stringsSeparatedBySemicolons(line, 11).get(2) + ":00" +
                        "', '" + angabe + "');"
                );
            } else if (line.substring(0, 13).equals("KAMPFGERICHT:")) {
                int abschnitt = Integer.parseInt(stringsSeparatedBySemicolons(line, 14).get(0));
                String position = stringsSeparatedBySemicolons(line, 14).get(1);
                String nachname = stringsSeparatedByCommas(stringsSeparatedBySemicolons(line, 14).get(2), 0).get(0);
                String vorname = stringsSeparatedByCommas(stringsSeparatedBySemicolons(line, 14).get(2), 0).get(1);
                String verein = stringsSeparatedBySemicolons(line, 14).get(3);
                ResultSet resultSet = statement.executeQuery("SELECT * FROM Verein WHERE Bezeichnung = '" + verein + "';");
                if (!resultSet.next()) {
                    statement.execute("INSERT INTO Verein(Bezeichnung) VALUES ('" + verein + "');");
                }

                resultSet = statement.executeQuery("SELECT * FROM Kampfrichter WHERE Nachname = '" + nachname +
                        "' AND Vorname = '" + vorname + "' AND VID = (SELECT VID FROM Verein WHERE Bezeichnung = '" +
                        verein + "');"
                );
                if (!resultSet.next()) {
                    System.out.println("INSERT INTO Kampfrichter(Nachname, Vorname, VID) VALUES ('" +
                            nachname + "', '" + vorname + "', (SELECT VID FROM Verein WHERE Bezeichnung = '" +
                            verein + "'));");
                    statement.execute("INSERT INTO Kampfrichter(Nachname, Vorname, VID) VALUES ('" +
                            nachname + "', '" + vorname + "', (SELECT VID FROM Verein WHERE Bezeichnung = '" +
                            verein + "'));"
                    );
                }
                System.out.println("INSERT INTO Kampfgericht(Abschnittsnummer, Position, KID) VALUES (" +
                        abschnitt + ", '" + position + "', (SELECT KID FROM Kampfrichter WHERE Nachname = '" + nachname +
                        "' AND Vorname = '" + vorname + "' AND VID = (SELECT VID FROM Verein WHERE Bezeichnung = '" +
                        verein + "')));");
                statement.execute("INSERT INTO Kampfgericht(Abschnittsnummer, Position, KID) VALUES (" +
                        abschnitt + ", '" + position + "', (SELECT KID FROM Kampfrichter WHERE Nachname = '" + nachname +
                        "' AND Vorname = '" + vorname + "' AND VID = (SELECT VID FROM Verein WHERE Bezeichnung = '" +
                        verein + "')));"
                );
            }
        } catch (StringIndexOutOfBoundsException e) {

        } catch (NullPointerException e) {

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> stringsSeparatedBySemicolons(String string, int start) {
        ArrayList<String> stringsSeparatedBySemicolons = new ArrayList<>();
        String stringUntilSemicolon = "";
        for (int i = start; i < string.length(); i++) {
            if (string.charAt(i) != ';') {
                stringUntilSemicolon = stringUntilSemicolon + string.charAt(i);
            } else {
                stringsSeparatedBySemicolons.add(stringUntilSemicolon);
                stringUntilSemicolon = "";
            }
        }
        stringsSeparatedBySemicolons.add(stringUntilSemicolon);
        return stringsSeparatedBySemicolons;
    }

    private ArrayList<String> stringsSeparatedByCommas(String string, int start) {
        ArrayList<String> stringsSeparatedByCommas = new ArrayList<>();
        String stringUntilComma = "";
        for (int i = start; i < string.length(); i++) {
            if (string.charAt(i) != ',') {
                stringUntilComma = stringUntilComma + string.charAt(i);
            } else {
                stringsSeparatedByCommas.add(stringUntilComma);
                stringUntilComma = "";
            }
        }
        stringsSeparatedByCommas.add(stringUntilComma);
        return stringsSeparatedByCommas;
    }
}
