package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel budgets through their life cycle and keeps a journal of what it did. */
data class ParcelBudgetCoordinator(
    var store: ParcelBudgetStore,
    var validator: ParcelBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("parcel budget $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelBudgetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelBudgetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newParcelBudgetCoordinator(store: ParcelBudgetStore, clock: Clock): ParcelBudgetCoordinator {
    return ParcelBudgetCoordinator(
        store = store,
        validator = defaultParcelBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
