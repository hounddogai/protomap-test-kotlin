package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global inspection templates through their life cycle and keeps a journal of what it did. */
data class GlobalInspectionTemplateDesk(
    var store: GlobalInspectionTemplateStore,
    var validator: GlobalInspectionTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalInspectionTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global inspection template $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalInspectionTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global inspection template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalInspectionTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global inspection template $code not found")
        val result = engine.compute(item)
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

fun newGlobalInspectionTemplateDesk(store: GlobalInspectionTemplateStore, clock: Clock, flags: Registry): GlobalInspectionTemplateDesk {
    return GlobalInspectionTemplateDesk(
        store = store,
        validator = defaultGlobalInspectionTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalInspectionTemplateEngine(flags),
    )
}
