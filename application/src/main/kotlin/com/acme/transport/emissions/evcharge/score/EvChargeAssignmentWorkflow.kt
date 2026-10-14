package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge assignments through their life cycle and keeps a journal of what it did. */
data class EvChargeAssignmentDesk(
    var store: EvChargeAssignmentStore,
    var validator: EvChargeAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("ev charge assignment $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newEvChargeAssignmentDesk(store: EvChargeAssignmentStore, clock: Clock): EvChargeAssignmentDesk {
    return EvChargeAssignmentDesk(
        store = store,
        validator = defaultEvChargeAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
