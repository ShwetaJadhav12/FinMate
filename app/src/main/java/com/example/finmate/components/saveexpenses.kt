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
        val db = FirebaseFirestore.getInstance()
        // Generate a new document reference with auto ID
        val docRef = db.collection("users")
            .document(uid)
            .collection("expenses")
            .document()

        // Set the expense with the generated ID
        val expenseWithId = expense.copy(id = docRef.id)

        docRef.set(expenseWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("User not logged in"))
    }
}



fun fetchExpenses(
    onSuccess: (List<Expenses>) -> Unit,
    onFailure: (Exception) -> Unit = {}
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(uid)
        .collection("expenses")
        .get()
        .addOnSuccessListener { snapshot ->
            val expenseList = snapshot.map { doc ->
                Expenses(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    amount = doc.getString("amount") ?: "",
                    category = doc.getString("category") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: ""
                )
            }
            onSuccess(expenseList)
        }
        .addOnFailureListener { e -> onFailure(e) }
}
