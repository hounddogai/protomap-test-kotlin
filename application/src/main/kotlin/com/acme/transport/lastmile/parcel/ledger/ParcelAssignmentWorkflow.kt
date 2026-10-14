package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves parcel assignments through their life cycle and keeps a journal of what it did. */
data class ParcelAssignmentDesk(
    var store: ParcelAssignmentStore,
    var validator: ParcelAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("parcel assignment $code not found")
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

fun newParcelAssignmentDesk(store: ParcelAssignmentStore, clock: Clock, flags: Registry): ParcelAssignmentDesk {
    return ParcelAssignmentDesk(
        store = store,
        validator = defaultParcelAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelAssignmentEngine(flags),
    )
}
