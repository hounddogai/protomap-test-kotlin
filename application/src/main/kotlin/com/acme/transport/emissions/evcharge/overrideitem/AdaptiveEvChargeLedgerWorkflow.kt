package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.clock.Clock

/** Moves adaptive ev charge ledgers through their life cycle and keeps a journal of what it did. */
data class AdaptiveEvChargeLedgerCoordinator(
    var store: AdaptiveEvChargeLedgerStore,
    var validator: AdaptiveEvChargeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive ev charge ledger $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEvChargeLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive ev charge ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEvChargeLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAdaptiveEvChargeLedgerCoordinator(store: AdaptiveEvChargeLedgerStore, clock: Clock): AdaptiveEvChargeLedgerCoordinator {
    return AdaptiveEvChargeLedgerCoordinator(
        store = store,
        validator = defaultAdaptiveEvChargeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
