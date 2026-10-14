package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.safety.clock.Clock

/** Moves global coaching segments through their life cycle and keeps a journal of what it did. */
data class GlobalCoachingSegmentService(
    var store: GlobalCoachingSegmentStore,
    var validator: GlobalCoachingSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCoachingSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global coaching segment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCoachingSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global coaching segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCoachingSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global coaching segment $code not found")
        val result = engine.compute(item)
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

fun newGlobalCoachingSegmentService(store: GlobalCoachingSegmentStore, clock: Clock): GlobalCoachingSegmentService {
    return GlobalCoachingSegmentService(
        store = store,
        validator = defaultGlobalCoachingSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCoachingSegmentEngine(),
    )
}
