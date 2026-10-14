package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity windows through their life cycle and keeps a journal of what it did. */
data class CapacityWindowWorkflow(
    var store: CapacityWindowStore,
    var validator: CapacityWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity window $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCapacityWindowWorkflow(store: CapacityWindowStore, clock: Clock): CapacityWindowWorkflow {
    return CapacityWindowWorkflow(
        store = store,
        validator = defaultCapacityWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
