package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard claims through their life cycle and keeps a journal of what it did. */
data class TrailerYardClaimDesk(
    var store: TrailerYardClaimStore,
    var validator: TrailerYardClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer yard claim $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardClaimStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardClaimStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("trailer yard claim $code not found")
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

fun newTrailerYardClaimDesk(store: TrailerYardClaimStore, clock: Clock): TrailerYardClaimDesk {
    return TrailerYardClaimDesk(
        store = store,
        validator = defaultTrailerYardClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardClaimEngine(),
    )
}
