package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled rest break targets through their life cycle and keeps a journal of what it did. */
data class ScheduledRestBreakTargetCoordinator(
    var store: ScheduledRestBreakTargetStore,
    var validator: ScheduledRestBreakTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRestBreakTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled rest break target $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRestBreakTargetStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled rest break target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRestBreakTargetStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled rest break target $code not found")
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

fun newScheduledRestBreakTargetCoordinator(store: ScheduledRestBreakTargetStore, clock: Clock): ScheduledRestBreakTargetCoordinator {
    return ScheduledRestBreakTargetCoordinator(
        store = store,
        validator = defaultScheduledRestBreakTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRestBreakTargetEngine(),
    )
}
