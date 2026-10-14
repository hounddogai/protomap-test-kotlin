package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.clock.Clock

/** Moves weighted broker plans through their life cycle and keeps a journal of what it did. */
data class WeightedBrokerPlanService(
    var store: WeightedBrokerPlanStore,
    var validator: WeightedBrokerPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedBrokerPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted broker plan $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedBrokerPlanStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted broker plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedBrokerPlanStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted broker plan $code not found")
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

fun newWeightedBrokerPlanService(store: WeightedBrokerPlanStore, clock: Clock): WeightedBrokerPlanService {
    return WeightedBrokerPlanService(
        store = store,
        validator = defaultWeightedBrokerPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedBrokerPlanEngine(),
    )
}
