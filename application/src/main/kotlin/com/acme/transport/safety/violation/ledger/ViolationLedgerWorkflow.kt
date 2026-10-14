package com.acme.transport.safety.violation.ledger

import com.acme.transport.safety.clock.Clock

/** Moves violation ledgers through their life cycle and keeps a journal of what it did. */
data class ViolationLedgerManager(
    var store: ViolationLedgerStore,
    var validator: ViolationLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newViolationLedgerManager(store: ViolationLedgerStore, clock: Clock): ViolationLedgerManager {
    return ViolationLedgerManager(
        store = store,
        validator = defaultViolationLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
