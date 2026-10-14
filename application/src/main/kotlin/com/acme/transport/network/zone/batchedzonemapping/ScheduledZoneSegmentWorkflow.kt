package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves scheduled zone segments through their life cycle and keeps a journal of what it did. */
data class ScheduledZoneSegmentManager(
    var store: ScheduledZoneSegmentStore,
    var validator: ScheduledZoneSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledZoneSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled zone segment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledZoneSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled zone segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledZoneSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled zone segment $code not found")
        val result = engine.evaluate(item)
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

fun newScheduledZoneSegmentManager(store: ScheduledZoneSegmentStore, clock: Clock, flags: Registry): ScheduledZoneSegmentManager {
    return ScheduledZoneSegmentManager(
        store = store,
        validator = defaultScheduledZoneSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledZoneSegmentEngine(flags),
    )
}
