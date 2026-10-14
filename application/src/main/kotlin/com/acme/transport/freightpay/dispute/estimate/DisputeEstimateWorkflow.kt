package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute estimates through their life cycle and keeps a journal of what it did. */
data class DisputeEstimateWorkflow(
    var store: DisputeEstimateStore,
    var validator: DisputeEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dispute estimate $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newDisputeEstimateWorkflow(store: DisputeEstimateStore, clock: Clock, flags: Registry): DisputeEstimateWorkflow {
    return DisputeEstimateWorkflow(
        store = store,
        validator = defaultDisputeEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeEstimateEngine(flags),
    )
}
