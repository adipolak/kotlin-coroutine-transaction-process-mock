package bank_transaction_2


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.*
import bank_transactions.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

import log
import printHeader

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


    printHeader("start")
    testGenerateIds(requestForTransactions)

    runBlocking {
        testGenerateIdWithCoroutines(requestForTransactions)

        testGenerateIdWithDispatcher(requestForTransactions)

        testGenerateIdWithChannel(requestForTransactions)

        testGenerateIdWithDispatcherAndChannel(requestForTransactions)

    }


    // ### popular types of channels:

    val channel1 = Channel<TransactionsType>(capacity = Channel.RENDEZVOUS)
    val channel2 = Channel<TransactionsType>(capacity = Channel.CONFLATED)
    val channel3 = Channel<TransactionsType>(capacity = 10)
    val channel4 = Channel<TransactionsType>(capacity = Channel.UNLIMITED)

    // ### Context examples
    Dispatchers.Default
    Dispatchers.IO
    Dispatchers.Main
    Dispatchers.Unconfined


}

private suspend fun testGenerateIdWithDispatcherAndChannel(requestForTransactions: List<TransactionsType>) {
    val time = measureTimeMillis {
        generateIdWithChannelAndDispatcher(requestForTransactions)
    }
    printHeader(" generateId with channel and dispatcher $time")
}

private suspend fun testGenerateIdWithChannel(requestForTransactions: List<TransactionsType>) {
    val time = measureTimeMillis {
        generateIdWithChannel(transactions = requestForTransactions)
    }
    printHeader(" generateId with channel $time")
}

private suspend fun testGenerateIdWithDispatcher(requestForTransactions: List<TransactionsType>) {
    val time = measureTimeMillis {
        generateIdWithCoroutineAndDispatcher(requestForTransactions)
    }

    printHeader("generateId with coroutine and dispatcher $time")
}

private suspend fun testGenerateIdWithCoroutines(requestForTransactions: List<TransactionsType>) {
    val time = measureTimeMillis {
        generateIdWithCoroutine(requestForTransactions)
    }
    printHeader("generateId with coroutine $time")
}

fun testGenerateIds(requestForTransactions: List<TransactionsType>) {
//    val output = Array<ProceededTransactionPayment>(requestForTransactions.size)
    val time = measureTimeMillis {
        val output = generateIds(requestForTransactions)
        log("$output")
    }

    printHeader(" generateId $time")
}

suspend fun generateIdWithChannelAndDispatcher(transactions: List<TransactionsType>) {

    generateIdWithChannel(Dispatchers.Default, transactions)
}


private suspend fun generateIdWithChannel(
    context: CoroutineContext = EmptyCoroutineContext,
    transactions: List<TransactionsType>
) {

    val ordersChannel = Channel<TransactionsType>()
    val job = CoroutineScope(context).launch {
        for (order in transactions) {
            ordersChannel.send(order)
        }
        ordersChannel.close()
    }

    coroutineScope {
        for (i in 1..2) { // as many as we would like / can create
            launch(CoroutineName("generateId_routine_$i") + context) { generateIdWithCoroutineAndChannel(ordersChannel) }
        }
    }


}


private suspend fun generateIdWithCoroutineAndChannel(transactionChannel: ReceiveChannel<TransactionsType>) {

    for (transaction in transactionChannel) {
        generateIdSuspend(transaction)
    }

}

private suspend fun generateIdWithCoroutine(transactions: List<TransactionsType>):Array<ProceededTransactionPayment> {

    log(" ###### generateId with coroutines ######")
    val deferred = coroutineScope {
        async<Array<ProceededTransactionPayment>> (CoroutineName("generateId_routine")) {
            generateIdsSuspend(transactions)
        }
    }
    return deferred.await()
}

private suspend fun generateIdWithCoroutineAndDispatcher(transactions: List<TransactionsType>):Array<ProceededTransactionPayment> {
    log(" ###### generateId with coroutines and dispatcher ######")

    val deferred = coroutineScope {
        async(Dispatchers.Default + CoroutineName("generateId_routine_with_thread_pool")) {
            generateIdsSuspend(transactions)
        }
    }
    return deferred.await()
}

suspend fun generateIdsSuspend(transactions: List<TransactionsType>):Array<ProceededTransactionPayment> {

    return transactions.map { it ->  generateIdSuspend(it) }.toTypedArray()
}

suspend fun generateIdSuspend(transaction: TransactionsType): ProceededTransactionPayment {

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

suspend fun generatePremiumId(): String {
    delay(10)
    return getRandStr(9)
}

suspend fun generateRegularId(): String {
    delay(5)
    return getRandStr(5)
}

private fun getRandStr(size: Int): String {
    return (1..size)
        .map { i -> Random.nextInt(0, 9) }
        .joinToString("")
}




