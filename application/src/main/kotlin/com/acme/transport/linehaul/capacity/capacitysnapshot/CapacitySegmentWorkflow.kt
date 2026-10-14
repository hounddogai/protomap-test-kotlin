package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity segments through their life cycle and keeps a journal of what it did. */
data class CapacitySegmentCoordinator(
    var store: CapacitySegmentStore,
    var validator: CapacitySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity segment $code not found")
        validator.requireValid(item)
        item.moveTo(CapacitySegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacitySegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCapacitySegmentCoordinator(store: CapacitySegmentStore, clock: Clock): CapacitySegmentCoordinator {
    return CapacitySegmentCoordinator(
        store = store,
        validator = defaultCapacitySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
