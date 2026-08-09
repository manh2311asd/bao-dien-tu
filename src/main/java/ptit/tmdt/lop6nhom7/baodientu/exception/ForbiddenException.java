package ptit.tmdt.lop6nhom7.baodientu.exception;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String accountIsBanned) {
    super(accountIsBanned);
  }
}
