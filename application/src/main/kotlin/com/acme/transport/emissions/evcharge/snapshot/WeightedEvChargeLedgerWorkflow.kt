package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.clock.Clock

/** Moves weighted ev charge ledgers through their life cycle and keeps a journal of what it did. */
data class WeightedEvChargeLedgerCoordinator(
    var store: WeightedEvChargeLedgerStore,
    var validator: WeightedEvChargeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted ev charge ledger $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedEvChargeLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted ev charge ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedEvChargeLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newWeightedEvChargeLedgerCoordinator(store: WeightedEvChargeLedgerStore, clock: Clock): WeightedEvChargeLedgerCoordinator {
    return WeightedEvChargeLedgerCoordinator(
        store = store,
        validator = defaultWeightedEvChargeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
