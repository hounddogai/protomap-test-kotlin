package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection cycles through their life cycle and keeps a journal of what it did. */
data class InspectionCycleManager(
    var store: InspectionCycleStore,
    var validator: InspectionCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection cycle $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection cycle $code not found")
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

fun newInspectionCycleManager(store: InspectionCycleStore, clock: Clock, flags: Registry): InspectionCycleManager {
    return InspectionCycleManager(
        store = store,
        validator = defaultInspectionCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionCycleEngine(flags),
    )
}
