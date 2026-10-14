package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves utilization overrides through their life cycle and keeps a journal of what it did. */
data class UtilizationOverrideDesk(
    var store: UtilizationOverrideStore,
    var validator: UtilizationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("utilization override $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization override $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newUtilizationOverrideDesk(store: UtilizationOverrideStore, clock: Clock, flags: Registry): UtilizationOverrideDesk {
    return UtilizationOverrideDesk(
        store = store,
        validator = defaultUtilizationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationOverrideEngine(flags),
    )
}
