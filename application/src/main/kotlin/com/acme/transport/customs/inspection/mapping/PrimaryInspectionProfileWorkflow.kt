package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.clock.Clock

/** Moves primary inspection profiles through their life cycle and keeps a journal of what it did. */
data class PrimaryInspectionProfileDesk(
    var store: PrimaryInspectionProfileStore,
    var validator: PrimaryInspectionProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary inspection profile $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryInspectionProfileStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary inspection profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryInspectionProfileStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPrimaryInspectionProfileDesk(store: PrimaryInspectionProfileStore, clock: Clock): PrimaryInspectionProfileDesk {
    return PrimaryInspectionProfileDesk(
        store = store,
        validator = defaultPrimaryInspectionProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
