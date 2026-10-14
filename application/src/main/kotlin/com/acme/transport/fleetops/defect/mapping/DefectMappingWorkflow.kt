package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.fleetops.clock.Clock

/** Moves defect mappings through their life cycle and keeps a journal of what it did. */
data class DefectMappingWorkflow(
    var store: DefectMappingStore,
    var validator: DefectMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("defect mapping $code not found")
        validator.requireValid(item)
        item.moveTo(DefectMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("defect mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDefectMappingWorkflow(store: DefectMappingStore, clock: Clock): DefectMappingWorkflow {
    return DefectMappingWorkflow(
        store = store,
        validator = defaultDefectMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
