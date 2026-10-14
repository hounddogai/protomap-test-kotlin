package com.acme.transport.fleetops.tire.target

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire thresholds through their life cycle and keeps a journal of what it did. */
data class TireThresholdCoordinator(
    var store: TireThresholdStore,
    var validator: TireThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tire threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TireThresholdStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tire threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireThresholdStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tire threshold $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTireThresholdCoordinator(store: TireThresholdStore, clock: Clock, flags: Registry): TireThresholdCoordinator {
    return TireThresholdCoordinator(
        store = store,
        validator = defaultTireThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireThresholdEngine(flags),
    )
}
