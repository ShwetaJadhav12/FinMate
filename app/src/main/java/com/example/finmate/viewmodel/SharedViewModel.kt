package com.example.finmate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

class SharedMonthViewModelnew : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthId(): String {
        val month = _selectedMonth.value
        return "${month.year}-${month.monthValue.toString().padStart(2, '0')}"
    }
}
class BudgetViewModel : ViewModel() {

    private val _budgetAmount = MutableStateFlow<Double?>(null)
    val budgetAmount: StateFlow<Double?> = _budgetAmount.asStateFlow()

    fun fetchBudgetForMonth(monthId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("summary_data")
            .document(monthId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _budgetAmount.value = doc.getDouble("amount") ?: 0.0
                } else {
                    _budgetAmount.value = 0.0
                }
            }
    }

    fun saveBudget(monthId: String, amount: Double, startDate: String, endDate: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = hashMapOf(
            "amount" to amount,
            "startDate" to startDate,
            "endDate" to endDate
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("summary_data")
            .document(monthId)
            .set(data)
            .addOnSuccessListener {
                _budgetAmount.value = amount
            }
    }
}