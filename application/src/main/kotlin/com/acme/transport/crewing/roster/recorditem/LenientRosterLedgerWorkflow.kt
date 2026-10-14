package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.clock.Clock

/** Moves lenient roster ledgers through their life cycle and keeps a journal of what it did. */
data class LenientRosterLedgerService(
    var store: LenientRosterLedgerStore,
    var validator: LenientRosterLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient roster ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRosterLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient roster ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRosterLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientRosterLedgerService(store: LenientRosterLedgerStore, clock: Clock): LenientRosterLedgerService {
    return LenientRosterLedgerService(
        store = store,
        validator = defaultLenientRosterLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
