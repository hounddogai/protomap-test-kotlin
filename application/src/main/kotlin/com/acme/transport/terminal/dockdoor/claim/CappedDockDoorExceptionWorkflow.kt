package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves capped dock door exceptions through their life cycle and keeps a journal of what it did. */
data class CappedDockDoorExceptionDesk(
    var store: CappedDockDoorExceptionStore,
    var validator: CappedDockDoorExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDockDoorExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped dock door exception $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDockDoorExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dock door exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDockDoorExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped dock door exception $code not found")
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

fun newCappedDockDoorExceptionDesk(store: CappedDockDoorExceptionStore, clock: Clock, flags: Registry): CappedDockDoorExceptionDesk {
    return CappedDockDoorExceptionDesk(
        store = store,
        validator = defaultCappedDockDoorExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDockDoorExceptionEngine(flags),
    )
}
