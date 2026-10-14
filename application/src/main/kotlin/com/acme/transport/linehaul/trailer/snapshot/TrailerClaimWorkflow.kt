package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer claims through their life cycle and keeps a journal of what it did. */
data class TrailerClaimManager(
    var store: TrailerClaimStore,
    var validator: TrailerClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer claim $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerClaimStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerClaimStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("trailer claim $code not found")
        val result = engine.price(item)
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

fun newTrailerClaimManager(store: TrailerClaimStore, clock: Clock): TrailerClaimManager {
    return TrailerClaimManager(
        store = store,
        validator = defaultTrailerClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerClaimEngine(),
    )
}
