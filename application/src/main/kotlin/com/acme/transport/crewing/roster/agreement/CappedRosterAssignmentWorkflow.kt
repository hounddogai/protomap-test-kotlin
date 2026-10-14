package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped roster assignments through their life cycle and keeps a journal of what it did. */
data class CappedRosterAssignmentService(
    var store: CappedRosterAssignmentStore,
    var validator: CappedRosterAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedRosterAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped roster assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRosterAssignmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped roster assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRosterAssignmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped roster assignment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newCappedRosterAssignmentService(store: CappedRosterAssignmentStore, clock: Clock, flags: Registry): CappedRosterAssignmentService {
    return CappedRosterAssignmentService(
        store = store,
        validator = defaultCappedRosterAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedRosterAssignmentEngine(flags),
    )
}
