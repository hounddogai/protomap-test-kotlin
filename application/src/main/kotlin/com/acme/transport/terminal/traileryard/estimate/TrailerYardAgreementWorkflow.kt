package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard agreements through their life cycle and keeps a journal of what it did. */
data class TrailerYardAgreementWorkflow(
    var store: TrailerYardAgreementStore,
    var validator: TrailerYardAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer yard agreement $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardAgreementStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardAgreementStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTrailerYardAgreementWorkflow(store: TrailerYardAgreementStore, clock: Clock): TrailerYardAgreementWorkflow {
    return TrailerYardAgreementWorkflow(
        store = store,
        validator = defaultTrailerYardAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
