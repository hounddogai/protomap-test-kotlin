package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.clock.Clock

/** Moves primary hold ledgers through their life cycle and keeps a journal of what it did. */
data class PrimaryHoldLedgerWorkflow(
    var store: PrimaryHoldLedgerStore,
    var validator: PrimaryHoldLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary hold ledger $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryHoldLedgerStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary hold ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryHoldLedgerStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPrimaryHoldLedgerWorkflow(store: PrimaryHoldLedgerStore, clock: Clock): PrimaryHoldLedgerWorkflow {
    return PrimaryHoldLedgerWorkflow(
        store = store,
        validator = defaultPrimaryHoldLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
