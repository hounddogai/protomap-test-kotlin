package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.clock.Clock

/** Moves lenient lease return ledgers through their life cycle and keeps a journal of what it did. */
data class LenientLeaseReturnLedgerDesk(
    var store: LenientLeaseReturnLedgerStore,
    var validator: LenientLeaseReturnLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient lease return ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLeaseReturnLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient lease return ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLeaseReturnLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLenientLeaseReturnLedgerDesk(store: LenientLeaseReturnLedgerStore, clock: Clock): LenientLeaseReturnLedgerDesk {
    return LenientLeaseReturnLedgerDesk(
        store = store,
        validator = defaultLenientLeaseReturnLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
