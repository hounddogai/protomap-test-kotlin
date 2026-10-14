package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.clock.Clock

/** Moves pending accessorial budgets through their life cycle and keeps a journal of what it did. */
data class PendingAccessorialBudgetService(
    var store: PendingAccessorialBudgetStore,
    var validator: PendingAccessorialBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending accessorial budget $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAccessorialBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending accessorial budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAccessorialBudgetStatus.RETIRED)
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

fun newPendingAccessorialBudgetService(store: PendingAccessorialBudgetStore, clock: Clock): PendingAccessorialBudgetService {
    return PendingAccessorialBudgetService(
        store = store,
        validator = defaultPendingAccessorialBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
