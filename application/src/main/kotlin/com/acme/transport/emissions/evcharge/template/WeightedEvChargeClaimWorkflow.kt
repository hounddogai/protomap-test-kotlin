package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.clock.Clock

/** Moves weighted ev charge claims through their life cycle and keeps a journal of what it did. */
data class WeightedEvChargeClaimWorkflow(
    var store: WeightedEvChargeClaimStore,
    var validator: WeightedEvChargeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted ev charge claim $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedEvChargeClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted ev charge claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedEvChargeClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newWeightedEvChargeClaimWorkflow(store: WeightedEvChargeClaimStore, clock: Clock): WeightedEvChargeClaimWorkflow {
    return WeightedEvChargeClaimWorkflow(
        store = store,
        validator = defaultWeightedEvChargeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
