package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.fleetops.clock.Clock

/** Moves pending tire metrics through their life cycle and keeps a journal of what it did. */
data class PendingTireMetricDesk(
    var store: PendingTireMetricStore,
    var validator: PendingTireMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTireMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending tire metric $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTireMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tire metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTireMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending tire metric $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newPendingTireMetricDesk(store: PendingTireMetricStore, clock: Clock): PendingTireMetricDesk {
    return PendingTireMetricDesk(
        store = store,
        validator = defaultPendingTireMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTireMetricEngine(),
    )
}
