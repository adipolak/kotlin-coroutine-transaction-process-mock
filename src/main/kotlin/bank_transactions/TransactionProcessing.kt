package bank_transactions

import kotlinx.coroutines.yield
import log
import kotlin.system.measureTimeMillis

fun main() {

    val requestForTransactions = listOf(
        TransactionsType.Regular(Coin.AUS, 5.1),
        TransactionsType.Regular(Coin.USD, 2.1),
        TransactionsType.Premium(Coin.AUS, 6.1),
        TransactionsType.Premium(Coin.USD, 2.8),
        TransactionsType.Regular(Coin.AUS, 9.11),
        TransactionsType.Regular(Coin.USD, 150.0),
        TransactionsType.Premium(Coin.AUS, 7.8),
        TransactionsType.Premium(Coin.USD, 4.5)
    )

    val time = measureTimeMillis {
        generateIds(requestForTransactions)
    }
    log(" ###### Transaction $time")
}


fun generateIds(orders: List<TransactionsType>):Array<ProceededTransactionPayment> {

    return orders.map { it -> generateId(it) }.toTypedArray()
}


fun generateId(transaction: TransactionsType): ProceededTransactionPayment {

    log("Generating ID: $transaction")
    val processedTransaction = when (transaction) {

        is TransactionsType.Premium -> {
            ProceededTransactionPayment.Premium(
                generatePremiumId(),
                transaction,
                transaction.totalAmount(),
                transaction.coin
            )
        }
        is TransactionsType.Regular -> {
            ProceededTransactionPayment.Regular(
                generateRegularId(),
                transaction,
                transaction.totalAmount(),
                transaction.coin
            )
        }

    }
    log("Generated: $processedTransaction")
    return processedTransaction


}

fun generatePremiumId(): String {
    Thread.sleep(10)
    return (1..9)
        .map { i -> kotlin.random.Random.nextInt(0, 9) }
        .joinToString("")
}

fun generateRegularId(): String {
    Thread.sleep(5)
    return (1..5)
        .map { i -> kotlin.random.Random.nextInt(0, 9) }
        .joinToString("")
}








