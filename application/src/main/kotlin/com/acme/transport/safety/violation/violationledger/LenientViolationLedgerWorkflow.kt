package com.acme.transport.safety.violation.violationledger

import com.acme.transport.safety.clock.Clock

/** Moves lenient violation ledgers through their life cycle and keeps a journal of what it did. */
data class LenientViolationLedgerManager(
    var store: LenientViolationLedgerStore,
    var validator: LenientViolationLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient violation ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LenientViolationLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient violation ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientViolationLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientViolationLedgerManager(store: LenientViolationLedgerStore, clock: Clock): LenientViolationLedgerManager {
    return LenientViolationLedgerManager(
        store = store,
        validator = defaultLenientViolationLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
