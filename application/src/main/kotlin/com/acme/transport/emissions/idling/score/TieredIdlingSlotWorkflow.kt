package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.clock.Clock

/** Moves tiered idling slots through their life cycle and keeps a journal of what it did. */
data class TieredIdlingSlotWorkflow(
    var store: TieredIdlingSlotStore,
    var validator: TieredIdlingSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered idling slot $code not found")
        validator.requireValid(item)
        item.moveTo(TieredIdlingSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered idling slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredIdlingSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newTieredIdlingSlotWorkflow(store: TieredIdlingSlotStore, clock: Clock): TieredIdlingSlotWorkflow {
    return TieredIdlingSlotWorkflow(
        store = store,
        validator = defaultTieredIdlingSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
