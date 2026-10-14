package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection records through their life cycle and keeps a journal of what it did. */
data class InspectionRecordWorkflow(
    var store: InspectionRecordStore,
    var validator: InspectionRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection record $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection record $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newInspectionRecordWorkflow(store: InspectionRecordStore, clock: Clock, flags: Registry): InspectionRecordWorkflow {
    return InspectionRecordWorkflow(
        store = store,
        validator = defaultInspectionRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionRecordEngine(flags),
    )
}
