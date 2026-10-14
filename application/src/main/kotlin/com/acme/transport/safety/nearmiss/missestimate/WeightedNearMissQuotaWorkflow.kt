package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.safety.clock.Clock

/** Moves weighted near miss quotas through their life cycle and keeps a journal of what it did. */
data class WeightedNearMissQuotaWorkflow(
    var store: WeightedNearMissQuotaStore,
    var validator: WeightedNearMissQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedNearMissQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted near miss quota $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedNearMissQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted near miss quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedNearMissQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted near miss quota $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newWeightedNearMissQuotaWorkflow(store: WeightedNearMissQuotaStore, clock: Clock): WeightedNearMissQuotaWorkflow {
    return WeightedNearMissQuotaWorkflow(
        store = store,
        validator = defaultWeightedNearMissQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedNearMissQuotaEngine(),
    )
}
