package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.clock.Clock

/** Moves defect slots through their life cycle and keeps a journal of what it did. */
data class DefectSlotDesk(
    var store: DefectSlotStore,
    var validator: DefectSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect slot $code not found")
        validator.requireValid(item)
        item.moveTo(DefectSlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("defect slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectSlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newDefectSlotDesk(store: DefectSlotStore, clock: Clock): DefectSlotDesk {
    return DefectSlotDesk(
        store = store,
        validator = defaultDefectSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
