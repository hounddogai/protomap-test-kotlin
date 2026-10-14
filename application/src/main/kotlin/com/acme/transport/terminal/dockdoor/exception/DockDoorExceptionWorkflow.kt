package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.terminal.clock.Clock

/** Moves dock door exceptions through their life cycle and keeps a journal of what it did. */
data class DockDoorExceptionCoordinator(
    var store: DockDoorExceptionStore,
    var validator: DockDoorExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door exception $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorExceptionStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorExceptionStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door exception $code not found")
        val result = engine.run(item)
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

fun newDockDoorExceptionCoordinator(store: DockDoorExceptionStore, clock: Clock): DockDoorExceptionCoordinator {
    return DockDoorExceptionCoordinator(
        store = store,
        validator = defaultDockDoorExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorExceptionEngine(),
    )
}
