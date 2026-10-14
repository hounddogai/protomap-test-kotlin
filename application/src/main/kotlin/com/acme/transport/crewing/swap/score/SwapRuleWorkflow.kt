package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap rules through their life cycle and keeps a journal of what it did. */
data class SwapRuleWorkflow(
    var store: SwapRuleStore,
    var validator: SwapRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("swap rule $code not found")
        validator.requireValid(item)
        item.moveTo(SwapRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("swap rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("swap rule $code not found")
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

fun newSwapRuleWorkflow(store: SwapRuleStore, clock: Clock, flags: Registry): SwapRuleWorkflow {
    return SwapRuleWorkflow(
        store = store,
        validator = defaultSwapRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapRuleEngine(flags),
    )
}
