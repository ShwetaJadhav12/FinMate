import com.example.finmate.model.Expenses
import com.google.firebase.firestore.FirebaseFirestore

fun saveExpenseToFirestore(expense: Expenses, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("expenses")
        .add(expense)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}
