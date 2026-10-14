package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback tender assignments through their life cycle and keeps a journal of what it did. */
data class FallbackTenderAssignmentCoordinator(
    var store: FallbackTenderAssignmentStore,
    var validator: FallbackTenderAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback tender assignment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackTenderAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback tender assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackTenderAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newFallbackTenderAssignmentCoordinator(store: FallbackTenderAssignmentStore, clock: Clock): FallbackTenderAssignmentCoordinator {
    return FallbackTenderAssignmentCoordinator(
        store = store,
        validator = defaultFallbackTenderAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
