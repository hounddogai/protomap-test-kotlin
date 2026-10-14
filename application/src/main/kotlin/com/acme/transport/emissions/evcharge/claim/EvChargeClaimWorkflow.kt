package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge claims through their life cycle and keeps a journal of what it did. */
data class EvChargeClaimDesk(
    var store: EvChargeClaimStore,
    var validator: EvChargeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("ev charge claim $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("ev charge claim $code not found")
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

fun newEvChargeClaimDesk(store: EvChargeClaimStore, clock: Clock, flags: Registry): EvChargeClaimDesk {
    return EvChargeClaimDesk(
        store = store,
        validator = defaultEvChargeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeClaimEngine(flags),
    )
}
