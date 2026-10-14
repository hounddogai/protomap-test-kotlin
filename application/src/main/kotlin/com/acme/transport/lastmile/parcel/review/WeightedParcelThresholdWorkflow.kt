package com.acme.transport.lastmile.parcel.review

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted parcel thresholds through their life cycle and keeps a journal of what it did. */
data class WeightedParcelThresholdDesk(
    var store: WeightedParcelThresholdStore,
    var validator: WeightedParcelThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedParcelThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted parcel threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedParcelThresholdStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted parcel threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedParcelThresholdStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted parcel threshold $code not found")
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

fun newWeightedParcelThresholdDesk(store: WeightedParcelThresholdStore, clock: Clock): WeightedParcelThresholdDesk {
    return WeightedParcelThresholdDesk(
        store = store,
        validator = defaultWeightedParcelThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedParcelThresholdEngine(),
    )
}
