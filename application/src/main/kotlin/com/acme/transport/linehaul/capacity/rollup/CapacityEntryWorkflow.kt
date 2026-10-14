package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity entries through their life cycle and keeps a journal of what it did. */
data class CapacityEntryCoordinator(
    var store: CapacityEntryStore,
    var validator: CapacityEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capacity entry $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityEntryStatus.EXPIRED)
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

fun newCapacityEntryCoordinator(store: CapacityEntryStore, clock: Clock): CapacityEntryCoordinator {
    return CapacityEntryCoordinator(
        store = store,
        validator = defaultCapacityEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
