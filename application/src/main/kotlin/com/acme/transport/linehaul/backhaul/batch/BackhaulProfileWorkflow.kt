package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul profiles through their life cycle and keeps a journal of what it did. */
data class BackhaulProfileService(
    var store: BackhaulProfileStore,
    var validator: BackhaulProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul profile $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulProfileStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("backhaul profile $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newBackhaulProfileService(store: BackhaulProfileStore, clock: Clock, flags: Registry): BackhaulProfileService {
    return BackhaulProfileService(
        store = store,
        validator = defaultBackhaulProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulProfileEngine(flags),
    )
}
