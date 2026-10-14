package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.clock.Clock

/**
 * Moves pending inspection checkpoints through their life cycle and keeps a journal of what it did.
 */
data class PendingInspectionCheckpointService(
    var store: PendingInspectionCheckpointStore,
    var validator: PendingInspectionCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending inspection checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPendingInspectionCheckpointService(store: PendingInspectionCheckpointStore, clock: Clock): PendingInspectionCheckpointService {
    return PendingInspectionCheckpointService(
        store = store,
        validator = defaultPendingInspectionCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
