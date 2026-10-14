package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted utilization audits through their life cycle and keeps a journal of what it did. */
data class WeightedUtilizationAuditCoordinator(
    var store: WeightedUtilizationAuditStore,
    var validator: WeightedUtilizationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedUtilizationAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted utilization audit $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedUtilizationAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted utilization audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedUtilizationAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted utilization audit $code not found")
        val result = engine.compute(item)
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

fun newWeightedUtilizationAuditCoordinator(store: WeightedUtilizationAuditStore, clock: Clock): WeightedUtilizationAuditCoordinator {
    return WeightedUtilizationAuditCoordinator(
        store = store,
        validator = defaultWeightedUtilizationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedUtilizationAuditEngine(),
    )
}
