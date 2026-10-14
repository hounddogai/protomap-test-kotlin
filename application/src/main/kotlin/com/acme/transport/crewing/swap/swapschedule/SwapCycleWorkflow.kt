package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap cycles through their life cycle and keeps a journal of what it did. */
data class SwapCycleService(
    var store: SwapCycleStore,
    var validator: SwapCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("swap cycle $code not found")
        validator.requireValid(item)
        item.moveTo(SwapCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("swap cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("swap cycle $code not found")
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

fun newSwapCycleService(store: SwapCycleStore, clock: Clock, flags: Registry): SwapCycleService {
    return SwapCycleService(
        store = store,
        validator = defaultSwapCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapCycleEngine(flags),
    )
}
