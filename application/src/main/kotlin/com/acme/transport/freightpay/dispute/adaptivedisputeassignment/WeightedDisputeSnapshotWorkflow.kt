package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted dispute snapshots through their life cycle and keeps a journal of what it did. */
data class WeightedDisputeSnapshotDesk(
    var store: WeightedDisputeSnapshotStore,
    var validator: WeightedDisputeSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDisputeSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted dispute snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDisputeSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dispute snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDisputeSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted dispute snapshot $code not found")
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

fun newWeightedDisputeSnapshotDesk(store: WeightedDisputeSnapshotStore, clock: Clock, flags: Registry): WeightedDisputeSnapshotDesk {
    return WeightedDisputeSnapshotDesk(
        store = store,
        validator = defaultWeightedDisputeSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDisputeSnapshotEngine(flags),
    )
}
