package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default ev charge claims through their life cycle and keeps a journal of what it did. */
data class DefaultEvChargeClaimCoordinator(
    var store: DefaultEvChargeClaimStore,
    var validator: DefaultEvChargeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultEvChargeClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default ev charge claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEvChargeClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default ev charge claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEvChargeClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default ev charge claim $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultEvChargeClaimCoordinator(store: DefaultEvChargeClaimStore, clock: Clock, flags: Registry): DefaultEvChargeClaimCoordinator {
    return DefaultEvChargeClaimCoordinator(
        store = store,
        validator = defaultDefaultEvChargeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultEvChargeClaimEngine(flags),
    )
}
