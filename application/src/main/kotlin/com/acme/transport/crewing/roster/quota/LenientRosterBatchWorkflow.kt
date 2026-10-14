package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.clock.Clock

/** Moves lenient roster batches through their life cycle and keeps a journal of what it did. */
data class LenientRosterBatchCoordinator(
    var store: LenientRosterBatchStore,
    var validator: LenientRosterBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient roster batch $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRosterBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient roster batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRosterBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientRosterBatchCoordinator(store: LenientRosterBatchStore, clock: Clock): LenientRosterBatchCoordinator {
    return LenientRosterBatchCoordinator(
        store = store,
        validator = defaultLenientRosterBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
