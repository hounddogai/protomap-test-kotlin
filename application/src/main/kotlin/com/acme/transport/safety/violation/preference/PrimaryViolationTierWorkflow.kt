package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves primary violation tiers through their life cycle and keeps a journal of what it did. */
data class PrimaryViolationTierCoordinator(
    var store: PrimaryViolationTierStore,
    var validator: PrimaryViolationTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryViolationTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary violation tier $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryViolationTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary violation tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryViolationTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary violation tier $code not found")
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

fun newPrimaryViolationTierCoordinator(store: PrimaryViolationTierStore, clock: Clock, flags: Registry): PrimaryViolationTierCoordinator {
    return PrimaryViolationTierCoordinator(
        store = store,
        validator = defaultPrimaryViolationTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryViolationTierEngine(flags),
    )
}
