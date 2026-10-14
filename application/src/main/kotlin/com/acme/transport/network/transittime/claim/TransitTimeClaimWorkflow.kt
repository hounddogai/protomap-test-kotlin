package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves transit time claims through their life cycle and keeps a journal of what it did. */
data class TransitTimeClaimService(
    var store: TransitTimeClaimStore,
    var validator: TransitTimeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time claim $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("transit time claim $code not found")
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

fun newTransitTimeClaimService(store: TransitTimeClaimStore, clock: Clock, flags: Registry): TransitTimeClaimService {
    return TransitTimeClaimService(
        store = store,
        validator = defaultTransitTimeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeClaimEngine(flags),
    )
}
