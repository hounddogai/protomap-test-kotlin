package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.clock.Clock

/** Moves drop ledgers through their life cycle and keeps a journal of what it did. */
data class DropLedgerService(
    var store: DropLedgerStore,
    var validator: DropLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DropLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("drop ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("drop ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDropLedgerService(store: DropLedgerStore, clock: Clock): DropLedgerService {
    return DropLedgerService(
        store = store,
        validator = defaultDropLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropLedgerEngine(),
    )
}
