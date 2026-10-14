package com.acme.transport.linehaul.capacity.target

import com.acme.transport.linehaul.clock.Clock

/** Moves pending capacity entries through their life cycle and keeps a journal of what it did. */
data class PendingCapacityEntryDesk(
    var store: PendingCapacityEntryStore,
    var validator: PendingCapacityEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending capacity entry $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCapacityEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending capacity entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCapacityEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newPendingCapacityEntryDesk(store: PendingCapacityEntryStore, clock: Clock): PendingCapacityEntryDesk {
    return PendingCapacityEntryDesk(
        store = store,
        validator = defaultPendingCapacityEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
