package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.clock.Clock

/** Moves pending inspection slots through their life cycle and keeps a journal of what it did. */
data class PendingInspectionSlotManager(
    var store: PendingInspectionSlotStore,
    var validator: PendingInspectionSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingInspectionSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending inspection slot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending inspection slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingInspectionSlotManager(store: PendingInspectionSlotStore, clock: Clock): PendingInspectionSlotManager {
    return PendingInspectionSlotManager(
        store = store,
        validator = defaultPendingInspectionSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingInspectionSlotEngine(),
    )
}
