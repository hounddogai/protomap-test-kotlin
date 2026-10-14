package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity requests through their life cycle and keeps a journal of what it did. */
data class CapacityRequestWorkflow(
    var store: CapacityRequestStore,
    var validator: CapacityRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capacity request $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCapacityRequestWorkflow(store: CapacityRequestStore, clock: Clock): CapacityRequestWorkflow {
    return CapacityRequestWorkflow(
        store = store,
        validator = defaultCapacityRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
