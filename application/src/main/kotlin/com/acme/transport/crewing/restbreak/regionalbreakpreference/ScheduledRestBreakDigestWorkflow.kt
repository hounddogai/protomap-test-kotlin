package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled rest break digests through their life cycle and keeps a journal of what it did. */
data class ScheduledRestBreakDigestWorkflow(
    var store: ScheduledRestBreakDigestStore,
    var validator: ScheduledRestBreakDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRestBreakDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled rest break digest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRestBreakDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled rest break digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRestBreakDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled rest break digest $code not found")
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

fun newScheduledRestBreakDigestWorkflow(store: ScheduledRestBreakDigestStore, clock: Clock, flags: Registry): ScheduledRestBreakDigestWorkflow {
    return ScheduledRestBreakDigestWorkflow(
        store = store,
        validator = defaultScheduledRestBreakDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRestBreakDigestEngine(flags),
    )
}
