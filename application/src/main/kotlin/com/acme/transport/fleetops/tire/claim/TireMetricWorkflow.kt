package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire metrics through their life cycle and keeps a journal of what it did. */
data class TireMetricDesk(
    var store: TireMetricStore,
    var validator: TireMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tire metric $code not found")
        validator.requireValid(item)
        item.moveTo(TireMetricStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tire metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireMetricStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tire metric $code not found")
        val result = engine.evaluate(item)
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

fun newTireMetricDesk(store: TireMetricStore, clock: Clock, flags: Registry): TireMetricDesk {
    return TireMetricDesk(
        store = store,
        validator = defaultTireMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireMetricEngine(flags),
    )
}
