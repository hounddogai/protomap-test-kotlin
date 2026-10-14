package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection checkpoints through their life cycle and keeps a journal of what it did. */
data class InspectionCheckpointWorkflow(
    var store: InspectionCheckpointStore,
    var validator: InspectionCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection checkpoint $code not found")
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

fun newInspectionCheckpointWorkflow(store: InspectionCheckpointStore, clock: Clock): InspectionCheckpointWorkflow {
    return InspectionCheckpointWorkflow(
        store = store,
        validator = defaultInspectionCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionCheckpointEngine(),
    )
}
