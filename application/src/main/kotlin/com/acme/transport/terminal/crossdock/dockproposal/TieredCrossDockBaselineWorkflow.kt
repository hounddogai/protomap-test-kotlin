package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves tiered cross dock baselines through their life cycle and keeps a journal of what it did. */
data class TieredCrossDockBaselineWorkflow(
    var store: TieredCrossDockBaselineStore,
    var validator: TieredCrossDockBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCrossDockBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered cross dock baseline $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCrossDockBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cross dock baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCrossDockBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered cross dock baseline $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newTieredCrossDockBaselineWorkflow(store: TieredCrossDockBaselineStore, clock: Clock, flags: Registry): TieredCrossDockBaselineWorkflow {
    return TieredCrossDockBaselineWorkflow(
        store = store,
        validator = defaultTieredCrossDockBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCrossDockBaselineEngine(flags),
    )
}
