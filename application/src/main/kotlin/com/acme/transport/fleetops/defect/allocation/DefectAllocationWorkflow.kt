package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.clock.Clock

/** Moves defect allocations through their life cycle and keeps a journal of what it did. */
data class DefectAllocationService(
    var store: DefectAllocationStore,
    var validator: DefectAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("defect allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DefectAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("defect allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDefectAllocationService(store: DefectAllocationStore, clock: Clock): DefectAllocationService {
    return DefectAllocationService(
        store = store,
        validator = defaultDefectAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
