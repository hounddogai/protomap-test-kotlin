package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient attempt ledgers through their life cycle and keeps a journal of what it did. */
data class LenientAttemptLedgerService(
    var store: LenientAttemptLedgerStore,
    var validator: LenientAttemptLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient attempt ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAttemptLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient attempt ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAttemptLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newLenientAttemptLedgerService(store: LenientAttemptLedgerStore, clock: Clock): LenientAttemptLedgerService {
    return LenientAttemptLedgerService(
        store = store,
        validator = defaultLenientAttemptLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
