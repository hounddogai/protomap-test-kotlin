package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves lenient dwell checkpoints through their life cycle and keeps a journal of what it did. */
data class LenientDwellCheckpointManager(
    var store: LenientDwellCheckpointStore,
    var validator: LenientDwellCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDwellCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient dwell checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDwellCheckpointStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dwell checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDwellCheckpointStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient dwell checkpoint $code not found")
        val result = engine.evaluate(item)
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

fun newLenientDwellCheckpointManager(store: LenientDwellCheckpointStore, clock: Clock, flags: Registry): LenientDwellCheckpointManager {
    return LenientDwellCheckpointManager(
        store = store,
        validator = defaultLenientDwellCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDwellCheckpointEngine(flags),
    )
}
