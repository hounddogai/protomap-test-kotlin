package com.acme.transport.network.node.policy

import com.acme.transport.network.clock.Clock

/** Moves rolling node reservations through their life cycle and keeps a journal of what it did. */
data class RollingNodeReservationDesk(
    var store: RollingNodeReservationStore,
    var validator: RollingNodeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling node reservation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingNodeReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling node reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingNodeReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newRollingNodeReservationDesk(store: RollingNodeReservationStore, clock: Clock): RollingNodeReservationDesk {
    return RollingNodeReservationDesk(
        store = store,
        validator = defaultRollingNodeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
