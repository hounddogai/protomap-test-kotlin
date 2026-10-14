package com.acme.transport.customs.inspection.claim

import com.acme.transport.customs.clock.Clock

/** Moves inspection claims through their life cycle and keeps a journal of what it did. */
data class InspectionClaimManager(
    var store: InspectionClaimStore,
    var validator: InspectionClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection claim $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionClaimStatus.FAILED)
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

fun newInspectionClaimManager(store: InspectionClaimStore, clock: Clock): InspectionClaimManager {
    return InspectionClaimManager(
        store = store,
        validator = defaultInspectionClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
