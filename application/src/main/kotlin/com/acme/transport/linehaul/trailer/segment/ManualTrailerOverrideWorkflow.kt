package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves manual trailer overrides through their life cycle and keeps a journal of what it did. */
data class ManualTrailerOverrideWorkflow(
    var store: ManualTrailerOverrideStore,
    var validator: ManualTrailerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTrailerOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual trailer override $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTrailerOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual trailer override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTrailerOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual trailer override $code not found")
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

fun newManualTrailerOverrideWorkflow(store: ManualTrailerOverrideStore, clock: Clock, flags: Registry): ManualTrailerOverrideWorkflow {
    return ManualTrailerOverrideWorkflow(
        store = store,
        validator = defaultManualTrailerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTrailerOverrideEngine(flags),
    )
}
