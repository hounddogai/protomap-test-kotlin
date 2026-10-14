package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves fallback relay claims through their life cycle and keeps a journal of what it did. */
data class FallbackRelayClaimManager(
    var store: FallbackRelayClaimStore,
    var validator: FallbackRelayClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRelayClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback relay claim $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRelayClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback relay claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRelayClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback relay claim $code not found")
        val result = engine.price(item)
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

fun newFallbackRelayClaimManager(store: FallbackRelayClaimStore, clock: Clock, flags: Registry): FallbackRelayClaimManager {
    return FallbackRelayClaimManager(
        store = store,
        validator = defaultFallbackRelayClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRelayClaimEngine(flags),
    )
}
