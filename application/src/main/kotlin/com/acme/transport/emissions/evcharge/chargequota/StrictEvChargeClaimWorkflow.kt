package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.clock.Clock

/** Moves strict ev charge claims through their life cycle and keeps a journal of what it did. */
data class StrictEvChargeClaimWorkflow(
    var store: StrictEvChargeClaimStore,
    var validator: StrictEvChargeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictEvChargeClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict ev charge claim $code not found")
        validator.requireValid(item)
        item.moveTo(StrictEvChargeClaimStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict ev charge claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictEvChargeClaimStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict ev charge claim $code not found")
        val result = engine.compute(item)
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

fun newStrictEvChargeClaimWorkflow(store: StrictEvChargeClaimStore, clock: Clock): StrictEvChargeClaimWorkflow {
    return StrictEvChargeClaimWorkflow(
        store = store,
        validator = defaultStrictEvChargeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictEvChargeClaimEngine(),
    )
}
