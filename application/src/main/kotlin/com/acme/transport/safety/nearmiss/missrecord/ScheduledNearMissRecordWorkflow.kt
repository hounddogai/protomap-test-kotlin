package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scheduled near miss records through their life cycle and keeps a journal of what it did. */
data class ScheduledNearMissRecordManager(
    var store: ScheduledNearMissRecordStore,
    var validator: ScheduledNearMissRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledNearMissRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled near miss record $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNearMissRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled near miss record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNearMissRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled near miss record $code not found")
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

fun newScheduledNearMissRecordManager(store: ScheduledNearMissRecordStore, clock: Clock, flags: Registry): ScheduledNearMissRecordManager {
    return ScheduledNearMissRecordManager(
        store = store,
        validator = defaultScheduledNearMissRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledNearMissRecordEngine(flags),
    )
}
