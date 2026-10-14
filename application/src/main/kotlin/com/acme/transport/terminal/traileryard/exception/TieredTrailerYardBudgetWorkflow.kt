package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.terminal.clock.Clock

/** Moves tiered trailer yard budgets through their life cycle and keeps a journal of what it did. */
data class TieredTrailerYardBudgetService(
    var store: TieredTrailerYardBudgetStore,
    var validator: TieredTrailerYardBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredTrailerYardBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered trailer yard budget $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTrailerYardBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered trailer yard budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTrailerYardBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered trailer yard budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newTieredTrailerYardBudgetService(store: TieredTrailerYardBudgetStore, clock: Clock): TieredTrailerYardBudgetService {
    return TieredTrailerYardBudgetService(
        store = store,
        validator = defaultTieredTrailerYardBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredTrailerYardBudgetEngine(),
    )
}
