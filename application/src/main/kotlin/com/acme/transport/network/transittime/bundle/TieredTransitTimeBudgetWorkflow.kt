package com.acme.transport.network.transittime.bundle

import com.acme.transport.network.clock.Clock

/** Moves tiered transit time budgets through their life cycle and keeps a journal of what it did. */
data class TieredTransitTimeBudgetCoordinator(
    var store: TieredTransitTimeBudgetStore,
    var validator: TieredTransitTimeBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredTransitTimeBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered transit time budget $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTransitTimeBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered transit time budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTransitTimeBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered transit time budget $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newTieredTransitTimeBudgetCoordinator(store: TieredTransitTimeBudgetStore, clock: Clock): TieredTransitTimeBudgetCoordinator {
    return TieredTransitTimeBudgetCoordinator(
        store = store,
        validator = defaultTieredTransitTimeBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredTransitTimeBudgetEngine(),
    )
}
