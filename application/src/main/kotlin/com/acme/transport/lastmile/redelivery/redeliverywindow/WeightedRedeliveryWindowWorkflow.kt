package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted redelivery windows through their life cycle and keeps a journal of what it did. */
data class WeightedRedeliveryWindowService(
    var store: WeightedRedeliveryWindowStore,
    var validator: WeightedRedeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedRedeliveryWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted redelivery window $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRedeliveryWindowStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted redelivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRedeliveryWindowStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted redelivery window $code not found")
        val result = engine.price(item)
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

fun newWeightedRedeliveryWindowService(store: WeightedRedeliveryWindowStore, clock: Clock): WeightedRedeliveryWindowService {
    return WeightedRedeliveryWindowService(
        store = store,
        validator = defaultWeightedRedeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedRedeliveryWindowEngine(),
    )
}
