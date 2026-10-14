package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.terminal.clock.Clock

/** Moves strict dock door batches through their life cycle and keeps a journal of what it did. */
data class StrictDockDoorBatchService(
    var store: StrictDockDoorBatchStore,
    var validator: StrictDockDoorBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict dock door batch $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDockDoorBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dock door batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDockDoorBatchStatus.RETIRED)
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

fun newStrictDockDoorBatchService(store: StrictDockDoorBatchStore, clock: Clock): StrictDockDoorBatchService {
    return StrictDockDoorBatchService(
        store = store,
        validator = defaultStrictDockDoorBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
