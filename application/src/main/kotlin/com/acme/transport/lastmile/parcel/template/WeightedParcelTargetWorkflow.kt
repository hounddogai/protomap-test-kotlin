package com.acme.transport.lastmile.parcel.template

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted parcel targets through their life cycle and keeps a journal of what it did. */
data class WeightedParcelTargetDesk(
    var store: WeightedParcelTargetStore,
    var validator: WeightedParcelTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedParcelTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted parcel target $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedParcelTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted parcel target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedParcelTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted parcel target $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newWeightedParcelTargetDesk(store: WeightedParcelTargetStore, clock: Clock): WeightedParcelTargetDesk {
    return WeightedParcelTargetDesk(
        store = store,
        validator = defaultWeightedParcelTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedParcelTargetEngine(),
    )
}
