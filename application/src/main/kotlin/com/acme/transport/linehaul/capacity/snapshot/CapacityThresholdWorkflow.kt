package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity thresholds through their life cycle and keeps a journal of what it did. */
data class CapacityThresholdWorkflow(
    var store: CapacityThresholdStore,
    var validator: CapacityThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capacity threshold $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newCapacityThresholdWorkflow(store: CapacityThresholdStore, clock: Clock): CapacityThresholdWorkflow {
    return CapacityThresholdWorkflow(
        store = store,
        validator = defaultCapacityThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
