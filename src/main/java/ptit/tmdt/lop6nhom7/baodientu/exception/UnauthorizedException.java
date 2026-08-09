package ptit.tmdt.lop6nhom7.baodientu.exception;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String invalidCredentials) {
    super(invalidCredentials);
  }
}
