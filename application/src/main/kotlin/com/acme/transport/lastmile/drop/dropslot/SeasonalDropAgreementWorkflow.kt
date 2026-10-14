package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal drop agreements through their life cycle and keeps a journal of what it did. */
data class SeasonalDropAgreementCoordinator(
    var store: SeasonalDropAgreementStore,
    var validator: SeasonalDropAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal drop agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDropAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal drop agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDropAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newSeasonalDropAgreementCoordinator(store: SeasonalDropAgreementStore, clock: Clock): SeasonalDropAgreementCoordinator {
    return SeasonalDropAgreementCoordinator(
        store = store,
        validator = defaultSeasonalDropAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
