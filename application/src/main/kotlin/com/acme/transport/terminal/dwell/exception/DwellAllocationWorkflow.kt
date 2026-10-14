package com.acme.transport.terminal.dwell.exception

import com.acme.transport.terminal.clock.Clock

/** Moves dwell allocations through their life cycle and keeps a journal of what it did. */
data class DwellAllocationCoordinator(
    var store: DwellAllocationStore,
    var validator: DwellAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DwellAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newDwellAllocationCoordinator(store: DwellAllocationStore, clock: Clock): DwellAllocationCoordinator {
    return DwellAllocationCoordinator(
        store = store,
        validator = defaultDwellAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
