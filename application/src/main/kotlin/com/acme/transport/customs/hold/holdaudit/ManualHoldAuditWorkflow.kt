package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual hold audits through their life cycle and keeps a journal of what it did. */
data class ManualHoldAuditCoordinator(
    var store: ManualHoldAuditStore,
    var validator: ManualHoldAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualHoldAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual hold audit $code not found")
        validator.requireValid(item)
        item.moveTo(ManualHoldAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual hold audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualHoldAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual hold audit $code not found")
        val result = engine.evaluate(item)
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

fun newManualHoldAuditCoordinator(store: ManualHoldAuditStore, clock: Clock, flags: Registry): ManualHoldAuditCoordinator {
    return ManualHoldAuditCoordinator(
        store = store,
        validator = defaultManualHoldAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualHoldAuditEngine(flags),
    )
}
