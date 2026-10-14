package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.fleetops.clock.Clock

/** Moves primary defect targets through their life cycle and keeps a journal of what it did. */
data class PrimaryDefectTargetManager(
    var store: PrimaryDefectTargetStore,
    var validator: PrimaryDefectTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary defect target $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDefectTargetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary defect target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDefectTargetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newPrimaryDefectTargetManager(store: PrimaryDefectTargetStore, clock: Clock): PrimaryDefectTargetManager {
    return PrimaryDefectTargetManager(
        store = store,
        validator = defaultPrimaryDefectTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
