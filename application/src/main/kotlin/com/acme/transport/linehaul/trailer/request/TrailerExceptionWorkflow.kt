package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer exceptions through their life cycle and keeps a journal of what it did. */
data class TrailerExceptionCoordinator(
    var store: TrailerExceptionStore,
    var validator: TrailerExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer exception $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerExceptionStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerExceptionStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("trailer exception $code not found")
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

fun newTrailerExceptionCoordinator(store: TrailerExceptionStore, clock: Clock, flags: Registry): TrailerExceptionCoordinator {
    return TrailerExceptionCoordinator(
        store = store,
        validator = defaultTrailerExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerExceptionEngine(flags),
    )
}
