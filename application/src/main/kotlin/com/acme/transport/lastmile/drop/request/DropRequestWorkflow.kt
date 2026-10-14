package com.acme.transport.lastmile.drop.request

import com.acme.transport.lastmile.clock.Clock

/** Moves drop requests through their life cycle and keeps a journal of what it did. */
data class DropRequestCoordinator(
    var store: DropRequestStore,
    var validator: DropRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop request $code not found")
        validator.requireValid(item)
        item.moveTo(DropRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("drop request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDropRequestCoordinator(store: DropRequestStore, clock: Clock): DropRequestCoordinator {
    return DropRequestCoordinator(
        store = store,
        validator = defaultDropRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
