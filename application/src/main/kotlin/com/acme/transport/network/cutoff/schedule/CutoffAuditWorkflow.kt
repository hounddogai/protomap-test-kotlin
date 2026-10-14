package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves cutoff audits through their life cycle and keeps a journal of what it did. */
data class CutoffAuditCoordinator(
    var store: CutoffAuditStore,
    var validator: CutoffAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cutoff audit $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("cutoff audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newCutoffAuditCoordinator(store: CutoffAuditStore, clock: Clock, flags: Registry): CutoffAuditCoordinator {
    return CutoffAuditCoordinator(
        store = store,
        validator = defaultCutoffAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffAuditEngine(flags),
    )
}
