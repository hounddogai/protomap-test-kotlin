package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.clock.Clock

/** Moves adaptive scorecard ledgers through their life cycle and keeps a journal of what it did. */
data class AdaptiveScorecardLedgerDesk(
    var store: AdaptiveScorecardLedgerStore,
    var validator: AdaptiveScorecardLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive scorecard ledger $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveScorecardLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive scorecard ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveScorecardLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newAdaptiveScorecardLedgerDesk(store: AdaptiveScorecardLedgerStore, clock: Clock): AdaptiveScorecardLedgerDesk {
    return AdaptiveScorecardLedgerDesk(
        store = store,
        validator = defaultAdaptiveScorecardLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
