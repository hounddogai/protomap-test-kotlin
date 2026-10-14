package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted dispute rules through their life cycle and keeps a journal of what it did. */
data class WeightedDisputeRuleWorkflow(
    var store: WeightedDisputeRuleStore,
    var validator: WeightedDisputeRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted dispute rule $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDisputeRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dispute rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDisputeRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newWeightedDisputeRuleWorkflow(store: WeightedDisputeRuleStore, clock: Clock): WeightedDisputeRuleWorkflow {
    return WeightedDisputeRuleWorkflow(
        store = store,
        validator = defaultWeightedDisputeRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
