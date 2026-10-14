package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves rolling camera event audits through their life cycle and keeps a journal of what it did. */
data class RollingCameraEventAuditCoordinator(
    var store: RollingCameraEventAuditStore,
    var validator: RollingCameraEventAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingCameraEventAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling camera event audit $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCameraEventAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling camera event audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCameraEventAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling camera event audit $code not found")
        val result = engine.run(item)
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

fun newRollingCameraEventAuditCoordinator(store: RollingCameraEventAuditStore, clock: Clock, flags: Registry): RollingCameraEventAuditCoordinator {
    return RollingCameraEventAuditCoordinator(
        store = store,
        validator = defaultRollingCameraEventAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingCameraEventAuditEngine(flags),
    )
}
