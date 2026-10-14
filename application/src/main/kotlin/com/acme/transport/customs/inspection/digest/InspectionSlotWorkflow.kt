package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.clock.Clock

/** Moves inspection slots through their life cycle and keeps a journal of what it did. */
data class InspectionSlotManager(
    var store: InspectionSlotStore,
    var validator: InspectionSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection slot $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newInspectionSlotManager(store: InspectionSlotStore, clock: Clock): InspectionSlotManager {
    return InspectionSlotManager(
        store = store,
        validator = defaultInspectionSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
