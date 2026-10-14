package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.clock.Clock

/** Moves relay budgets through their life cycle and keeps a journal of what it did. */
data class RelayBudgetService(
    var store: RelayBudgetStore,
    var validator: RelayBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay budget $code not found")
        validator.requireValid(item)
        item.moveTo(RelayBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("relay budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayBudgetStatus.RETIRED)
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

fun newRelayBudgetService(store: RelayBudgetStore, clock: Clock): RelayBudgetService {
    return RelayBudgetService(
        store = store,
        validator = defaultRelayBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
