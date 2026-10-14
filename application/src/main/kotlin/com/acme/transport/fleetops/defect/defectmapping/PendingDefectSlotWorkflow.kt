package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.clock.Clock

/** Moves pending defect slots through their life cycle and keeps a journal of what it did. */
data class PendingDefectSlotManager(
    var store: PendingDefectSlotStore,
    var validator: PendingDefectSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending defect slot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDefectSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending defect slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDefectSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newPendingDefectSlotManager(store: PendingDefectSlotStore, clock: Clock): PendingDefectSlotManager {
    return PendingDefectSlotManager(
        store = store,
        validator = defaultPendingDefectSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
