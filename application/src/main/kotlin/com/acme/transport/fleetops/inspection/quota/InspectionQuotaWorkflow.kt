package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection quotas through their life cycle and keeps a journal of what it did. */
data class InspectionQuotaService(
    var store: InspectionQuotaStore,
    var validator: InspectionQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection quota $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("inspection quota $code not found")
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

fun newInspectionQuotaService(store: InspectionQuotaStore, clock: Clock, flags: Registry): InspectionQuotaService {
    return InspectionQuotaService(
        store = store,
        validator = defaultInspectionQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionQuotaEngine(flags),
    )
}
