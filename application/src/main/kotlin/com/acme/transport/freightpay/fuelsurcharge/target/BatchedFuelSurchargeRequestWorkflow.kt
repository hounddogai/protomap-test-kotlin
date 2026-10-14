package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves batched fuel surcharge requests through their life cycle and keeps a journal of what it did.
 */
data class BatchedFuelSurchargeRequestDesk(
    var store: BatchedFuelSurchargeRequestStore,
    var validator: BatchedFuelSurchargeRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedFuelSurchargeRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched fuel surcharge request $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedFuelSurchargeRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched fuel surcharge request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedFuelSurchargeRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched fuel surcharge request $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newBatchedFuelSurchargeRequestDesk(store: BatchedFuelSurchargeRequestStore, clock: Clock): BatchedFuelSurchargeRequestDesk {
    return BatchedFuelSurchargeRequestDesk(
        store = store,
        validator = defaultBatchedFuelSurchargeRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedFuelSurchargeRequestEngine(),
    )
}
