package com.acme.transport.terminal.sortation.policy

import com.acme.transport.terminal.clock.Clock

/** Moves tiered sortation rules through their life cycle and keeps a journal of what it did. */
data class TieredSortationRuleCoordinator(
    var store: TieredSortationRuleStore,
    var validator: TieredSortationRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered sortation rule $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSortationRuleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered sortation rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSortationRuleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTieredSortationRuleCoordinator(store: TieredSortationRuleStore, clock: Clock): TieredSortationRuleCoordinator {
    return TieredSortationRuleCoordinator(
        store = store,
        validator = defaultTieredSortationRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
