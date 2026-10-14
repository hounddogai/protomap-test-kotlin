package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.clock.Clock

/** Moves capped idling windows through their life cycle and keeps a journal of what it did. */
data class CappedIdlingWindowManager(
    var store: CappedIdlingWindowStore,
    var validator: CappedIdlingWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped idling window $code not found")
        validator.requireValid(item)
        item.moveTo(CappedIdlingWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped idling window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedIdlingWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCappedIdlingWindowManager(store: CappedIdlingWindowStore, clock: Clock): CappedIdlingWindowManager {
    return CappedIdlingWindowManager(
        store = store,
        validator = defaultCappedIdlingWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
