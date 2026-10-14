package com.acme.transport.emissions.idling.proposal

import com.acme.transport.emissions.clock.Clock

/** Moves idling allocations through their life cycle and keeps a journal of what it did. */
data class IdlingAllocationCoordinator(
    var store: IdlingAllocationStore,
    var validator: IdlingAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling allocation $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingAllocationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("idling allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingAllocationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newIdlingAllocationCoordinator(store: IdlingAllocationStore, clock: Clock): IdlingAllocationCoordinator {
    return IdlingAllocationCoordinator(
        store = store,
        validator = defaultIdlingAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
