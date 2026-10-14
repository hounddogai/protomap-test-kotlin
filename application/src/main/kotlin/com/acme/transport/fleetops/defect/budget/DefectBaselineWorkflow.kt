package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves defect baselines through their life cycle and keeps a journal of what it did. */
data class DefectBaselineManager(
    var store: DefectBaselineStore,
    var validator: DefectBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("defect baseline $code not found")
        validator.requireValid(item)
        item.moveTo(DefectBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("defect baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDefectBaselineManager(store: DefectBaselineStore, clock: Clock): DefectBaselineManager {
    return DefectBaselineManager(
        store = store,
        validator = defaultDefectBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
