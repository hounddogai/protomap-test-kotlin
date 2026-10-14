package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.safety.clock.Clock

/** Moves capped camera event ledgers through their life cycle and keeps a journal of what it did. */
data class CappedCameraEventLedgerWorkflow(
    var store: CappedCameraEventLedgerStore,
    var validator: CappedCameraEventLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped camera event ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCameraEventLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped camera event ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCameraEventLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newCappedCameraEventLedgerWorkflow(store: CappedCameraEventLedgerStore, clock: Clock): CappedCameraEventLedgerWorkflow {
    return CappedCameraEventLedgerWorkflow(
        store = store,
        validator = defaultCappedCameraEventLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
