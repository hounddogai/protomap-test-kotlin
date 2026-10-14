package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves pending parcel overrides through their life cycle and keeps a journal of what it did. */
data class PendingParcelOverrideWorkflow(
    var store: PendingParcelOverrideStore,
    var validator: PendingParcelOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending parcel override $code not found")
        validator.requireValid(item)
        item.moveTo(PendingParcelOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending parcel override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingParcelOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPendingParcelOverrideWorkflow(store: PendingParcelOverrideStore, clock: Clock): PendingParcelOverrideWorkflow {
    return PendingParcelOverrideWorkflow(
        store = store,
        validator = defaultPendingParcelOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
