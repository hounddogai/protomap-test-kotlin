package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.fleetops.clock.Clock

/** Moves manual utilization variances through their life cycle and keeps a journal of what it did. */
data class ManualUtilizationVarianceWorkflow(
    var store: ManualUtilizationVarianceStore,
    var validator: ManualUtilizationVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualUtilizationVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual utilization variance $code not found")
        validator.requireValid(item)
        item.moveTo(ManualUtilizationVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual utilization variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualUtilizationVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual utilization variance $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newManualUtilizationVarianceWorkflow(store: ManualUtilizationVarianceStore, clock: Clock): ManualUtilizationVarianceWorkflow {
    return ManualUtilizationVarianceWorkflow(
        store = store,
        validator = defaultManualUtilizationVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualUtilizationVarianceEngine(),
    )
}
