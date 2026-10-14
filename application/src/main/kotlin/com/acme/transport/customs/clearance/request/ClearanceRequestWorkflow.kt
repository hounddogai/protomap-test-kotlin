package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.clock.Clock

/** Moves clearance requests through their life cycle and keeps a journal of what it did. */
data class ClearanceRequestManager(
    var store: ClearanceRequestStore,
    var validator: ClearanceRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("clearance request $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceRequestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceRequestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newClearanceRequestManager(store: ClearanceRequestStore, clock: Clock): ClearanceRequestManager {
    return ClearanceRequestManager(
        store = store,
        validator = defaultClearanceRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
