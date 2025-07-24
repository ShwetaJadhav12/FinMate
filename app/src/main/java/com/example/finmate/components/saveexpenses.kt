import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun saveExpenseToFirestore(
    expense: Expenses,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("User not logged in"))
    }
}
