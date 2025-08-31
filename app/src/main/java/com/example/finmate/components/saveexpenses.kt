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

        // Extract month and year from expense.date (assuming format: "dd/MM/yyyy")
        val monthYear = try {
            val parts = expense.date.split("/")
            if (parts.size == 3) {
                "${parts[1]}-${parts[2]}"  // e.g., "08-2025"
            } else {
                "unknown"
            }
        } catch (e: Exception) {
            "unknown"
        }

        // Reference to the monthly collection
        val docRef = db.collection("users")
            .document(uid)
            .collection("summary_data")
            .document(monthYear)
            .collection("expenses")
            .document()

        // Assign auto-generated ID
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