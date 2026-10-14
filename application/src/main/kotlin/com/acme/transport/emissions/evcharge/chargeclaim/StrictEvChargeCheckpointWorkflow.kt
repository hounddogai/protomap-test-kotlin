package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.clock.Clock

/** Moves strict ev charge checkpoints through their life cycle and keeps a journal of what it did. */
data class StrictEvChargeCheckpointManager(
    var store: StrictEvChargeCheckpointStore,
    var validator: StrictEvChargeCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict ev charge checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(StrictEvChargeCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict ev charge checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictEvChargeCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newStrictEvChargeCheckpointManager(store: StrictEvChargeCheckpointStore, clock: Clock): StrictEvChargeCheckpointManager {
    return StrictEvChargeCheckpointManager(
        store = store,
        validator = defaultStrictEvChargeCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
