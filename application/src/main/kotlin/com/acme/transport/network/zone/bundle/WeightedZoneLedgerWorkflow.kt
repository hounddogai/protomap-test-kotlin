package com.acme.transport.network.zone.bundle

import com.acme.transport.network.clock.Clock

/** Moves weighted zone ledgers through their life cycle and keeps a journal of what it did. */
data class WeightedZoneLedgerDesk(
    var store: WeightedZoneLedgerStore,
    var validator: WeightedZoneLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted zone ledger $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedZoneLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted zone ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedZoneLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newWeightedZoneLedgerDesk(store: WeightedZoneLedgerStore, clock: Clock): WeightedZoneLedgerDesk {
    return WeightedZoneLedgerDesk(
        store = store,
        validator = defaultWeightedZoneLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
