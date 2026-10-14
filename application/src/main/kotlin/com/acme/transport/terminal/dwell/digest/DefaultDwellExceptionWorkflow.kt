package com.acme.transport.terminal.dwell.digest

import com.acme.transport.terminal.clock.Clock

/** Moves default dwell exceptions through their life cycle and keeps a journal of what it did. */
data class DefaultDwellExceptionCoordinator(
    var store: DefaultDwellExceptionStore,
    var validator: DefaultDwellExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDwellExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default dwell exception $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDwellExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default dwell exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDwellExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default dwell exception $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultDwellExceptionCoordinator(store: DefaultDwellExceptionStore, clock: Clock): DefaultDwellExceptionCoordinator {
    return DefaultDwellExceptionCoordinator(
        store = store,
        validator = defaultDefaultDwellExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDwellExceptionEngine(),
    )
}
