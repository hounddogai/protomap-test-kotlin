package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute exceptions through their life cycle and keeps a journal of what it did. */
data class DisputeExceptionWorkflow(
    var store: DisputeExceptionStore,
    var validator: DisputeExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute exception $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dispute exception $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDisputeExceptionWorkflow(store: DisputeExceptionStore, clock: Clock): DisputeExceptionWorkflow {
    return DisputeExceptionWorkflow(
        store = store,
        validator = defaultDisputeExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeExceptionEngine(),
    )
}
