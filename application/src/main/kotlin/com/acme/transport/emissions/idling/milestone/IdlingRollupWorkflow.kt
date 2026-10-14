package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.clock.Clock

/** Moves idling rollups through their life cycle and keeps a journal of what it did. */
data class IdlingRollupDesk(
    var store: IdlingRollupStore,
    var validator: IdlingRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling rollup $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("idling rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newIdlingRollupDesk(store: IdlingRollupStore, clock: Clock): IdlingRollupDesk {
    return IdlingRollupDesk(
        store = store,
        validator = defaultIdlingRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
