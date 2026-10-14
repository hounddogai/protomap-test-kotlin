package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback dispute tiers through their life cycle and keeps a journal of what it did. */
data class FallbackDisputeTierDesk(
    var store: FallbackDisputeTierStore,
    var validator: FallbackDisputeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDisputeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback dispute tier $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDisputeTierStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dispute tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDisputeTierStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback dispute tier $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newFallbackDisputeTierDesk(store: FallbackDisputeTierStore, clock: Clock, flags: Registry): FallbackDisputeTierDesk {
    return FallbackDisputeTierDesk(
        store = store,
        validator = defaultFallbackDisputeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDisputeTierEngine(flags),
    )
}
