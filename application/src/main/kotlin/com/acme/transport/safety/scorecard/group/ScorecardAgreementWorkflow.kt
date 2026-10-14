package com.acme.transport.safety.scorecard.group

import com.acme.transport.safety.clock.Clock

/** Moves scorecard agreements through their life cycle and keeps a journal of what it did. */
data class ScorecardAgreementCoordinator(
    var store: ScorecardAgreementStore,
    var validator: ScorecardAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scorecard agreement $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newScorecardAgreementCoordinator(store: ScorecardAgreementStore, clock: Clock): ScorecardAgreementCoordinator {
    return ScorecardAgreementCoordinator(
        store = store,
        validator = defaultScorecardAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
