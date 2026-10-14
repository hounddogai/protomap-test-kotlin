package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves tiered parcel assignments through their life cycle and keeps a journal of what it did. */
data class TieredParcelAssignmentDesk(
    var store: TieredParcelAssignmentStore,
    var validator: TieredParcelAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredParcelAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered parcel assignment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredParcelAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered parcel assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredParcelAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered parcel assignment $code not found")
        val result = engine.run(item)
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

fun newTieredParcelAssignmentDesk(store: TieredParcelAssignmentStore, clock: Clock, flags: Registry): TieredParcelAssignmentDesk {
    return TieredParcelAssignmentDesk(
        store = store,
        validator = defaultTieredParcelAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredParcelAssignmentEngine(flags),
    )
}
