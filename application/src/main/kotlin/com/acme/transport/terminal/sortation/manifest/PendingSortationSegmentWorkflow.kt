package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves pending sortation segments through their life cycle and keeps a journal of what it did. */
data class PendingSortationSegmentManager(
    var store: PendingSortationSegmentStore,
    var validator: PendingSortationSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingSortationSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending sortation segment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingSortationSegmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending sortation segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingSortationSegmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending sortation segment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newPendingSortationSegmentManager(store: PendingSortationSegmentStore, clock: Clock, flags: Registry): PendingSortationSegmentManager {
    return PendingSortationSegmentManager(
        store = store,
        validator = defaultPendingSortationSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingSortationSegmentEngine(flags),
    )
}
