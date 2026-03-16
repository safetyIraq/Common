private void ensureUserInFirebase() {
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    if (firebaseUser == null) return;
    
    String uid = firebaseUser.getUid();
    String email = firebaseUser.getEmail();
    String name = firebaseUser.getDisplayName();
    
    if (name == null) name = "مستخدم";
    if (email == null) email = "user@example.com";
    
    DatabaseReference userRef = mDb.child("Users").child(uid);
    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()) {
                // المستخدم غير موجود → أضفه
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("uid", uid);
                userMap.put("displayName", name);
                userMap.put("username", name.toLowerCase().replace(" ", ""));
                userMap.put("email", email);
                userMap.put("profileImage", "");
                userMap.put("bio", "#لست صداعاً انا فكرة اكبر من رأسك.");
                
                userRef.setValue(userMap)
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(MainActivity.this, "✅ تم إضافة حسابك إلى Firebase", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> 
                            Toast.makeText(MainActivity.this, "❌ فشل الإضافة: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(MainActivity.this, "خطأ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
