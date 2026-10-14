package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves parcel entries through their life cycle and keeps a journal of what it did. */
data class ParcelEntryWorkflow(
    var store: ParcelEntryStore,
    var validator: ParcelEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel entry $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel entry $code not found")
        val result = engine.run(item)
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

fun newParcelEntryWorkflow(store: ParcelEntryStore, clock: Clock, flags: Registry): ParcelEntryWorkflow {
    return ParcelEntryWorkflow(
        store = store,
        validator = defaultParcelEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelEntryEngine(flags),
    )
}
