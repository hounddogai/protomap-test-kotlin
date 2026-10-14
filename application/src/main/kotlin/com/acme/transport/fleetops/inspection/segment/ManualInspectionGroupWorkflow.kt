package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual inspection groups through their life cycle and keeps a journal of what it did. */
data class ManualInspectionGroupWorkflow(
    var store: ManualInspectionGroupStore,
    var validator: ManualInspectionGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualInspectionGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        validator.requireValid(item)
        item.moveTo(ManualInspectionGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualInspectionGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        val result = engine.compute(item)
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

fun newManualInspectionGroupWorkflow(store: ManualInspectionGroupStore, clock: Clock, flags: Registry): ManualInspectionGroupWorkflow {
    return ManualInspectionGroupWorkflow(
        store = store,
        validator = defaultManualInspectionGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualInspectionGroupEngine(flags),
    )
}
