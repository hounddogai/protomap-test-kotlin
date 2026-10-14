package com.acme.transport.lastmile.locker.group

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled locker groups through their life cycle and keeps a journal of what it did. */
data class ScheduledLockerGroupDesk(
    var store: ScheduledLockerGroupStore,
    var validator: ScheduledLockerGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled locker group $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLockerGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled locker group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLockerGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newScheduledLockerGroupDesk(store: ScheduledLockerGroupStore, clock: Clock): ScheduledLockerGroupDesk {
    return ScheduledLockerGroupDesk(
        store = store,
        validator = defaultScheduledLockerGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
