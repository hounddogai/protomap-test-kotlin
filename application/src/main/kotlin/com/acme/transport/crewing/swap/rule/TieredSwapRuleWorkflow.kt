package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.clock.Clock

/** Moves tiered swap rules through their life cycle and keeps a journal of what it did. */
data class TieredSwapRuleWorkflow(
    var store: TieredSwapRuleStore,
    var validator: TieredSwapRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered swap rule $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSwapRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered swap rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSwapRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newTieredSwapRuleWorkflow(store: TieredSwapRuleStore, clock: Clock): TieredSwapRuleWorkflow {
    return TieredSwapRuleWorkflow(
        store = store,
        validator = defaultTieredSwapRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
