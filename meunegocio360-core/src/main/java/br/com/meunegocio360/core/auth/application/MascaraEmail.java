package br.com.meunegocio360.core.auth.application;

/** Mascara e-mails em logs de segurança (RNF-023, RNF-025, RNF-BE-44). Ex.: a***@dominio.com. */
final class MascaraEmail {

  private MascaraEmail() {}

  static String mascarar(String email) {
    if (email == null) {
      return "-";
    }
    int arroba = email.indexOf('@');
    if (arroba <= 0) {
      return "***";
    }
    return email.charAt(0) + "***" + email.substring(arroba);
  }
}
