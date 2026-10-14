package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer records through their life cycle and keeps a journal of what it did. */
data class TrailerRecordWorkflow(
    var store: TrailerRecordStore,
    var validator: TrailerRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer record $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("trailer record $code not found")
        val result = engine.compute(item)
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

fun newTrailerRecordWorkflow(store: TrailerRecordStore, clock: Clock, flags: Registry): TrailerRecordWorkflow {
    return TrailerRecordWorkflow(
        store = store,
        validator = defaultTrailerRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerRecordEngine(flags),
    )
}
