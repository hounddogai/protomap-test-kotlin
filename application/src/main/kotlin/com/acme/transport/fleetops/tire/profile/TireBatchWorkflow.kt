package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves tire batches through their life cycle and keeps a journal of what it did. */
data class TireBatchDesk(
    var store: TireBatchStore,
    var validator: TireBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire batch $code not found")
        validator.requireValid(item)
        item.moveTo(TireBatchStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireBatchStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTireBatchDesk(store: TireBatchStore, clock: Clock): TireBatchDesk {
    return TireBatchDesk(
        store = store,
        validator = defaultTireBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
