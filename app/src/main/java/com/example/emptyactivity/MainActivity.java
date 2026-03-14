// كود مشاركة الحساب
private void shareHussainAccount() {
    String myId = "حسابي في Hussain Ultra\n" +
                  "الاسم: مبرمج حسين\n" +
                  "رقم الهاتف: " + phoneIn.getText().toString() + "\n" +
                  "تم الإنشاء بواسطة تطبيق Hussain Ultra";

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, myId);
    startActivity(Intent.createChooser(shareIntent, "مشاركة حسابي عبر..."));
}
