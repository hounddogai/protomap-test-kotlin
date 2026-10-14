package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered swap windows through their life cycle and keeps a journal of what it did. */
data class TieredSwapWindowDesk(
    var store: TieredSwapWindowStore,
    var validator: TieredSwapWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredSwapWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered swap window $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSwapWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered swap window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSwapWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered swap window $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTieredSwapWindowDesk(store: TieredSwapWindowStore, clock: Clock, flags: Registry): TieredSwapWindowDesk {
    return TieredSwapWindowDesk(
        store = store,
        validator = defaultTieredSwapWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredSwapWindowEngine(flags),
    )
}
