package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves pending capacity metrics through their life cycle and keeps a journal of what it did. */
data class PendingCapacityMetricDesk(
    var store: PendingCapacityMetricStore,
    var validator: PendingCapacityMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingCapacityMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending capacity metric $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCapacityMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending capacity metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCapacityMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending capacity metric $code not found")
        val result = engine.compute(item)
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

fun newPendingCapacityMetricDesk(store: PendingCapacityMetricStore, clock: Clock, flags: Registry): PendingCapacityMetricDesk {
    return PendingCapacityMetricDesk(
        store = store,
        validator = defaultPendingCapacityMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingCapacityMetricEngine(flags),
    )
}
