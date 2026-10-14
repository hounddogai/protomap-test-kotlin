package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual defect targets through their life cycle and keeps a journal of what it did. */
data class ManualDefectTargetWorkflow(
    var store: ManualDefectTargetStore,
    var validator: ManualDefectTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDefectTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual defect target $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDefectTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual defect target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDefectTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual defect target $code not found")
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

fun newManualDefectTargetWorkflow(store: ManualDefectTargetStore, clock: Clock, flags: Registry): ManualDefectTargetWorkflow {
    return ManualDefectTargetWorkflow(
        store = store,
        validator = defaultManualDefectTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDefectTargetEngine(flags),
    )
}
