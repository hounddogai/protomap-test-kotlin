package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.terminal.clock.Clock

/** Moves default dock door preferences through their life cycle and keeps a journal of what it did. */
data class DefaultDockDoorPreferenceDesk(
    var store: DefaultDockDoorPreferenceStore,
    var validator: DefaultDockDoorPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDockDoorPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default dock door preference $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDockDoorPreferenceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default dock door preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDockDoorPreferenceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default dock door preference $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newDefaultDockDoorPreferenceDesk(store: DefaultDockDoorPreferenceStore, clock: Clock): DefaultDockDoorPreferenceDesk {
    return DefaultDockDoorPreferenceDesk(
        store = store,
        validator = defaultDefaultDockDoorPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDockDoorPreferenceEngine(),
    )
}
