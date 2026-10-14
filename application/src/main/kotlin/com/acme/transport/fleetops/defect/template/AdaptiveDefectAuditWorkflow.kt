package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive defect audits through their life cycle and keeps a journal of what it did. */
data class AdaptiveDefectAuditService(
    var store: AdaptiveDefectAuditStore,
    var validator: AdaptiveDefectAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDefectAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive defect audit $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDefectAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive defect audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDefectAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive defect audit $code not found")
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

fun newAdaptiveDefectAuditService(store: AdaptiveDefectAuditStore, clock: Clock, flags: Registry): AdaptiveDefectAuditService {
    return AdaptiveDefectAuditService(
        store = store,
        validator = defaultAdaptiveDefectAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDefectAuditEngine(flags),
    )
}
