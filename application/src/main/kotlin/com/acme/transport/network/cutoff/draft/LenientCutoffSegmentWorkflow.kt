package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves lenient cutoff segments through their life cycle and keeps a journal of what it did. */
data class LenientCutoffSegmentService(
    var store: LenientCutoffSegmentStore,
    var validator: LenientCutoffSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCutoffSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient cutoff segment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCutoffSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cutoff segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCutoffSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient cutoff segment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientCutoffSegmentService(store: LenientCutoffSegmentStore, clock: Clock, flags: Registry): LenientCutoffSegmentService {
    return LenientCutoffSegmentService(
        store = store,
        validator = defaultLenientCutoffSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCutoffSegmentEngine(flags),
    )
}
