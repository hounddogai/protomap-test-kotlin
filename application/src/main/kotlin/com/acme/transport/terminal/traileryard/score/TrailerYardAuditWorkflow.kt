package com.acme.transport.terminal.traileryard.score

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard audits through their life cycle and keeps a journal of what it did. */
data class TrailerYardAuditService(
    var store: TrailerYardAuditStore,
    var validator: TrailerYardAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer yard audit $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTrailerYardAuditService(store: TrailerYardAuditStore, clock: Clock): TrailerYardAuditService {
    return TrailerYardAuditService(
        store = store,
        validator = defaultTrailerYardAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
