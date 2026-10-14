package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel plans through their life cycle and keeps a journal of what it did. */
data class ParcelPlanCoordinator(
    var store: ParcelPlanStore,
    var validator: ParcelPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("parcel plan $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("parcel plan $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newParcelPlanCoordinator(store: ParcelPlanStore, clock: Clock): ParcelPlanCoordinator {
    return ParcelPlanCoordinator(
        store = store,
        validator = defaultParcelPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelPlanEngine(),
    )
}
