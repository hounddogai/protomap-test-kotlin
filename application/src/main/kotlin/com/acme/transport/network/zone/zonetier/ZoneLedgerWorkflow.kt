package com.acme.transport.network.zone.zonetier

import com.acme.transport.network.clock.Clock

/** Moves zone ledgers through their life cycle and keeps a journal of what it did. */
data class ZoneLedgerCoordinator(
    var store: ZoneLedgerStore,
    var validator: ZoneLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("zone ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newZoneLedgerCoordinator(store: ZoneLedgerStore, clock: Clock): ZoneLedgerCoordinator {
    return ZoneLedgerCoordinator(
        store = store,
        validator = defaultZoneLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneLedgerEngine(),
    )
}
