package bank_transactions


sealed class Coin {

    object USD : Coin() {
        override fun toString(): String = "USD"
    }

    object AUS : Coin() {
        override fun toString(): String = "AUS"
    }
}


sealed class TransactionsType {
    abstract fun transactionFee(): Double
    abstract fun totalAmount(): Double

    data class Premium(val coin: Coin, val amount: Double) : TransactionsType() {
        override fun transactionFee() = 1.02
        override fun totalAmount() = transactionFee() * amount
    }

    data class Regular(val coin: Coin, val amount: Double) : TransactionsType() {
        override fun transactionFee() = 1.01
        override fun totalAmount() = transactionFee() * amount
    }
}

sealed class ProceededTransactionPayment {
    data class Premium(val id: String, val type: TransactionsType.Premium, val totalAmount: Double, val coin: Coin) :
        ProceededTransactionPayment()

    data class Regular(val id: String, val type: TransactionsType.Regular, val totalAmount: Double, val coin: Coin) :
        ProceededTransactionPayment()
}

