package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.clock.Clock

/** Moves primary clearance variances through their life cycle and keeps a journal of what it did. */
data class PrimaryClearanceVarianceWorkflow(
    var store: PrimaryClearanceVarianceStore,
    var validator: PrimaryClearanceVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryClearanceVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary clearance variance $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryClearanceVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary clearance variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryClearanceVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary clearance variance $code not found")
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

fun newPrimaryClearanceVarianceWorkflow(store: PrimaryClearanceVarianceStore, clock: Clock): PrimaryClearanceVarianceWorkflow {
    return PrimaryClearanceVarianceWorkflow(
        store = store,
        validator = defaultPrimaryClearanceVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryClearanceVarianceEngine(),
    )
}
