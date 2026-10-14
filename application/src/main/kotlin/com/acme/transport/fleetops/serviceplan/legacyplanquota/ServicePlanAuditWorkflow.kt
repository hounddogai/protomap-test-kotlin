package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan audits through their life cycle and keeps a journal of what it did. */
data class ServicePlanAuditManager(
    var store: ServicePlanAuditStore,
    var validator: ServicePlanAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan audit $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan audit $code not found")
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

fun newServicePlanAuditManager(store: ServicePlanAuditStore, clock: Clock): ServicePlanAuditManager {
    return ServicePlanAuditManager(
        store = store,
        validator = defaultServicePlanAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanAuditEngine(),
    )
}
