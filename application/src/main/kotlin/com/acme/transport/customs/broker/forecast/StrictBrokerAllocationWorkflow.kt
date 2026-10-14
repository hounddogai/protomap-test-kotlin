package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.clock.Clock

/** Moves strict broker allocations through their life cycle and keeps a journal of what it did. */
data class StrictBrokerAllocationManager(
    var store: StrictBrokerAllocationStore,
    var validator: StrictBrokerAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict broker allocation $code not found")
        validator.requireValid(item)
        item.moveTo(StrictBrokerAllocationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict broker allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictBrokerAllocationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newStrictBrokerAllocationManager(store: StrictBrokerAllocationStore, clock: Clock): StrictBrokerAllocationManager {
    return StrictBrokerAllocationManager(
        store = store,
        validator = defaultStrictBrokerAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
