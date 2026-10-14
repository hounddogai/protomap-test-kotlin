package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.clock.Clock

/** Moves scheduled near miss segments through their life cycle and keeps a journal of what it did. */
data class ScheduledNearMissSegmentService(
    var store: ScheduledNearMissSegmentStore,
    var validator: ScheduledNearMissSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledNearMissSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled near miss segment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNearMissSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled near miss segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNearMissSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled near miss segment $code not found")
        val result = engine.price(item)
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

fun newScheduledNearMissSegmentService(store: ScheduledNearMissSegmentStore, clock: Clock): ScheduledNearMissSegmentService {
    return ScheduledNearMissSegmentService(
        store = store,
        validator = defaultScheduledNearMissSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledNearMissSegmentEngine(),
    )
}
