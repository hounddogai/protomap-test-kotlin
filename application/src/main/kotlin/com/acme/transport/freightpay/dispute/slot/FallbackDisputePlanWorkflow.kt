package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.clock.Clock

/** Moves fallback dispute plans through their life cycle and keeps a journal of what it did. */
data class FallbackDisputePlanManager(
    var store: FallbackDisputePlanStore,
    var validator: FallbackDisputePlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDisputePlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback dispute plan $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDisputePlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dispute plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDisputePlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback dispute plan $code not found")
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

fun newFallbackDisputePlanManager(store: FallbackDisputePlanStore, clock: Clock): FallbackDisputePlanManager {
    return FallbackDisputePlanManager(
        store = store,
        validator = defaultFallbackDisputePlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDisputePlanEngine(),
    )
}
