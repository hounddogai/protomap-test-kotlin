package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual availability segments through their life cycle and keeps a journal of what it did. */
data class ManualAvailabilitySegmentManager(
    var store: ManualAvailabilitySegmentStore,
    var validator: ManualAvailabilitySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAvailabilitySegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual availability segment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAvailabilitySegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual availability segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAvailabilitySegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual availability segment $code not found")
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

fun newManualAvailabilitySegmentManager(store: ManualAvailabilitySegmentStore, clock: Clock, flags: Registry): ManualAvailabilitySegmentManager {
    return ManualAvailabilitySegmentManager(
        store = store,
        validator = defaultManualAvailabilitySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAvailabilitySegmentEngine(flags),
    )
}
