package ch.postfinance.citrusframework.plugin.action;

final class TestInvocationException extends RuntimeException {

  private final String dialogMessage;

  public TestInvocationException(String dialogMessage) {
    this.dialogMessage = dialogMessage;
  }

  String getDialogMessage() {
    return dialogMessage;
  }
}
