package com.acme.transport.safety.coaching.template

import com.acme.transport.safety.clock.Clock

/** Moves primary coaching ledgers through their life cycle and keeps a journal of what it did. */
data class PrimaryCoachingLedgerWorkflow(
    var store: PrimaryCoachingLedgerStore,
    var validator: PrimaryCoachingLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary coaching ledger $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCoachingLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary coaching ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCoachingLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPrimaryCoachingLedgerWorkflow(store: PrimaryCoachingLedgerStore, clock: Clock): PrimaryCoachingLedgerWorkflow {
    return PrimaryCoachingLedgerWorkflow(
        store = store,
        validator = defaultPrimaryCoachingLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
