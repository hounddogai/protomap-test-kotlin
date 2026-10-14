package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary trailer claims through their life cycle and keeps a journal of what it did. */
data class PrimaryTrailerClaimService(
    var store: PrimaryTrailerClaimStore,
    var validator: PrimaryTrailerClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTrailerClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary trailer claim $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTrailerClaimStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary trailer claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTrailerClaimStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary trailer claim $code not found")
        val result = engine.price(item)
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

fun newPrimaryTrailerClaimService(store: PrimaryTrailerClaimStore, clock: Clock, flags: Registry): PrimaryTrailerClaimService {
    return PrimaryTrailerClaimService(
        store = store,
        validator = defaultPrimaryTrailerClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTrailerClaimEngine(flags),
    )
}
