package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock entries through their life cycle and keeps a journal of what it did. */
data class CrossDockEntryWorkflow(
    var store: CrossDockEntryStore,
    var validator: CrossDockEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cross dock entry $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("cross dock entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newCrossDockEntryWorkflow(store: CrossDockEntryStore, clock: Clock): CrossDockEntryWorkflow {
    return CrossDockEntryWorkflow(
        store = store,
        validator = defaultCrossDockEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockEntryEngine(),
    )
}
