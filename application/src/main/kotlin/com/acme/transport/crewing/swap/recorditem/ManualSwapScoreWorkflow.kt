package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual swap scores through their life cycle and keeps a journal of what it did. */
data class ManualSwapScoreCoordinator(
    var store: ManualSwapScoreStore,
    var validator: ManualSwapScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualSwapScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual swap score $code not found")
        validator.requireValid(item)
        item.moveTo(ManualSwapScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual swap score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualSwapScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual swap score $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newManualSwapScoreCoordinator(store: ManualSwapScoreStore, clock: Clock, flags: Registry): ManualSwapScoreCoordinator {
    return ManualSwapScoreCoordinator(
        store = store,
        validator = defaultManualSwapScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualSwapScoreEngine(flags),
    )
}
