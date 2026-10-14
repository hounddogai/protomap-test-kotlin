package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.terminal.clock.Clock

/** Moves legacy cross dock exceptions through their life cycle and keeps a journal of what it did. */
data class LegacyCrossDockExceptionWorkflow(
    var store: LegacyCrossDockExceptionStore,
    var validator: LegacyCrossDockExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyCrossDockExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy cross dock exception $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCrossDockExceptionStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy cross dock exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCrossDockExceptionStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy cross dock exception $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyCrossDockExceptionWorkflow(store: LegacyCrossDockExceptionStore, clock: Clock): LegacyCrossDockExceptionWorkflow {
    return LegacyCrossDockExceptionWorkflow(
        store = store,
        validator = defaultLegacyCrossDockExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyCrossDockExceptionEngine(),
    )
}
