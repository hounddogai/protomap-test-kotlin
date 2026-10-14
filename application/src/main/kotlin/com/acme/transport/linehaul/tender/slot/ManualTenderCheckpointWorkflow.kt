package com.acme.transport.linehaul.tender.slot

import com.acme.transport.linehaul.clock.Clock

/** Moves manual tender checkpoints through their life cycle and keeps a journal of what it did. */
data class ManualTenderCheckpointCoordinator(
    var store: ManualTenderCheckpointStore,
    var validator: ManualTenderCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual tender checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTenderCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tender checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTenderCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newManualTenderCheckpointCoordinator(store: ManualTenderCheckpointStore, clock: Clock): ManualTenderCheckpointCoordinator {
    return ManualTenderCheckpointCoordinator(
        store = store,
        validator = defaultManualTenderCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
