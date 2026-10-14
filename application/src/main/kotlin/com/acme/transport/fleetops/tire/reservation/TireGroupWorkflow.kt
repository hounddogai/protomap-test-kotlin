package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.clock.Clock

/** Moves tire groups through their life cycle and keeps a journal of what it did. */
data class TireGroupDesk(
    var store: TireGroupStore,
    var validator: TireGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire group $code not found")
        validator.requireValid(item)
        item.moveTo(TireGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tire group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTireGroupDesk(store: TireGroupStore, clock: Clock): TireGroupDesk {
    return TireGroupDesk(
        store = store,
        validator = defaultTireGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
