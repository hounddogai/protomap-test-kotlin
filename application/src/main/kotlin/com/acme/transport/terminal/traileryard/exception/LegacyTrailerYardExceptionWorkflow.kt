package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.terminal.clock.Clock

/**
 * Moves legacy trailer yard exceptions through their life cycle and keeps a journal of what it did.
 */
data class LegacyTrailerYardExceptionCoordinator(
    var store: LegacyTrailerYardExceptionStore,
    var validator: LegacyTrailerYardExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTrailerYardExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy trailer yard exception $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTrailerYardExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy trailer yard exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTrailerYardExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy trailer yard exception $code not found")
        val result = engine.evaluate(item)
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

fun newLegacyTrailerYardExceptionCoordinator(store: LegacyTrailerYardExceptionStore, clock: Clock): LegacyTrailerYardExceptionCoordinator {
    return LegacyTrailerYardExceptionCoordinator(
        store = store,
        validator = defaultLegacyTrailerYardExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTrailerYardExceptionEngine(),
    )
}
