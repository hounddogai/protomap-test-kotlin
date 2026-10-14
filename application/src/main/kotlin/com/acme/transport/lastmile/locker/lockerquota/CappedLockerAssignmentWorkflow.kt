package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.lastmile.clock.Clock

/** Moves capped locker assignments through their life cycle and keeps a journal of what it did. */
data class CappedLockerAssignmentDesk(
    var store: CappedLockerAssignmentStore,
    var validator: CappedLockerAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedLockerAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped locker assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLockerAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped locker assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLockerAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped locker assignment $code not found")
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

fun newCappedLockerAssignmentDesk(store: CappedLockerAssignmentStore, clock: Clock): CappedLockerAssignmentDesk {
    return CappedLockerAssignmentDesk(
        store = store,
        validator = defaultCappedLockerAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedLockerAssignmentEngine(),
    )
}
