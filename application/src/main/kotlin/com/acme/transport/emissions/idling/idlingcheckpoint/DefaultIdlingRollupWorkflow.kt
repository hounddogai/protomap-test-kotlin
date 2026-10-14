package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.clock.Clock

/** Moves default idling rollups through their life cycle and keeps a journal of what it did. */
data class DefaultIdlingRollupManager(
    var store: DefaultIdlingRollupStore,
    var validator: DefaultIdlingRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default idling rollup $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultIdlingRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default idling rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultIdlingRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultIdlingRollupManager(store: DefaultIdlingRollupStore, clock: Clock): DefaultIdlingRollupManager {
    return DefaultIdlingRollupManager(
        store = store,
        validator = defaultDefaultIdlingRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
