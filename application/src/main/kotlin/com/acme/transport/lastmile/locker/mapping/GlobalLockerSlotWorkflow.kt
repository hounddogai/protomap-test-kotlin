package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.lastmile.clock.Clock

/** Moves global locker slots through their life cycle and keeps a journal of what it did. */
data class GlobalLockerSlotManager(
    var store: GlobalLockerSlotStore,
    var validator: GlobalLockerSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global locker slot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalLockerSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global locker slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalLockerSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newGlobalLockerSlotManager(store: GlobalLockerSlotStore, clock: Clock): GlobalLockerSlotManager {
    return GlobalLockerSlotManager(
        store = store,
        validator = defaultGlobalLockerSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
