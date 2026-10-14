package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves global parcel assignments through their life cycle and keeps a journal of what it did. */
data class GlobalParcelAssignmentWorkflow(
    var store: GlobalParcelAssignmentStore,
    var validator: GlobalParcelAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalParcelAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global parcel assignment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalParcelAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global parcel assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalParcelAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global parcel assignment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newGlobalParcelAssignmentWorkflow(store: GlobalParcelAssignmentStore, clock: Clock, flags: Registry): GlobalParcelAssignmentWorkflow {
    return GlobalParcelAssignmentWorkflow(
        store = store,
        validator = defaultGlobalParcelAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalParcelAssignmentEngine(flags),
    )
}
