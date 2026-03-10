import utils.UtilEmail;

public class TestEmail {

    public static void main(String[] args) {
        int i = 1;
        UtilEmail.envoyerEmail(
                "manon_horan@hotmail.com", // à remplacer par son propre email
                "Test email",
                "Ce message est un test depuis JavaMail"+"de quantité : "+i
        );
    }
}
