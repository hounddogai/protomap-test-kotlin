package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.lastmile.clock.Clock

/** Moves locker groups through their life cycle and keeps a journal of what it did. */
data class LockerGroupCoordinator(
    var store: LockerGroupStore,
    var validator: LockerGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("locker group $code not found")
        validator.requireValid(item)
        item.moveTo(LockerGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("locker group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newLockerGroupCoordinator(store: LockerGroupStore, clock: Clock): LockerGroupCoordinator {
    return LockerGroupCoordinator(
        store = store,
        validator = defaultLockerGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
