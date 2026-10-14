package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.clock.Clock

/** Moves defect records through their life cycle and keeps a journal of what it did. */
data class DefectRecordManager(
    var store: DefectRecordStore,
    var validator: DefectRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("defect record $code not found")
        validator.requireValid(item)
        item.moveTo(DefectRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("defect record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDefectRecordManager(store: DefectRecordStore, clock: Clock): DefectRecordManager {
    return DefectRecordManager(
        store = store,
        validator = defaultDefectRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
