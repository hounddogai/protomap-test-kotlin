package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted dispute preferences through their life cycle and keeps a journal of what it did. */
data class WeightedDisputePreferenceManager(
    var store: WeightedDisputePreferenceStore,
    var validator: WeightedDisputePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDisputePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted dispute preference $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDisputePreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dispute preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDisputePreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted dispute preference $code not found")
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

fun newWeightedDisputePreferenceManager(store: WeightedDisputePreferenceStore, clock: Clock): WeightedDisputePreferenceManager {
    return WeightedDisputePreferenceManager(
        store = store,
        validator = defaultWeightedDisputePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDisputePreferenceEngine(),
    )
}
