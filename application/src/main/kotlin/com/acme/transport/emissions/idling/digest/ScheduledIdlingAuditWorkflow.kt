package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled idling audits through their life cycle and keeps a journal of what it did. */
data class ScheduledIdlingAuditCoordinator(
    var store: ScheduledIdlingAuditStore,
    var validator: ScheduledIdlingAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledIdlingAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled idling audit $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledIdlingAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled idling audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledIdlingAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled idling audit $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newScheduledIdlingAuditCoordinator(store: ScheduledIdlingAuditStore, clock: Clock, flags: Registry): ScheduledIdlingAuditCoordinator {
    return ScheduledIdlingAuditCoordinator(
        store = store,
        validator = defaultScheduledIdlingAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledIdlingAuditEngine(flags),
    )
}
