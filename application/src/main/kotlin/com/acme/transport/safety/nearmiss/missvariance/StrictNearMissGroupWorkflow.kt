package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict near miss groups through their life cycle and keeps a journal of what it did. */
data class StrictNearMissGroupCoordinator(
    var store: StrictNearMissGroupStore,
    var validator: StrictNearMissGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictNearMissGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict near miss group $code not found")
        validator.requireValid(item)
        item.moveTo(StrictNearMissGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict near miss group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictNearMissGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict near miss group $code not found")
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

fun newStrictNearMissGroupCoordinator(store: StrictNearMissGroupStore, clock: Clock, flags: Registry): StrictNearMissGroupCoordinator {
    return StrictNearMissGroupCoordinator(
        store = store,
        validator = defaultStrictNearMissGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictNearMissGroupEngine(flags),
    )
}
