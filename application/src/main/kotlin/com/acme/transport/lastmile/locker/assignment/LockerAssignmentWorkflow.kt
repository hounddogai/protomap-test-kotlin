package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves locker assignments through their life cycle and keeps a journal of what it did. */
data class LockerAssignmentManager(
    var store: LockerAssignmentStore,
    var validator: LockerAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("locker assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LockerAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("locker assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("locker assignment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newLockerAssignmentManager(store: LockerAssignmentStore, clock: Clock, flags: Registry): LockerAssignmentManager {
    return LockerAssignmentManager(
        store = store,
        validator = defaultLockerAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerAssignmentEngine(flags),
    )
}
