package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves legacy camera event claims through their life cycle and keeps a journal of what it did. */
data class LegacyCameraEventClaimWorkflow(
    var store: LegacyCameraEventClaimStore,
    var validator: LegacyCameraEventClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyCameraEventClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy camera event claim $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCameraEventClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy camera event claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCameraEventClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy camera event claim $code not found")
        val result = engine.compute(item)
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

fun newLegacyCameraEventClaimWorkflow(store: LegacyCameraEventClaimStore, clock: Clock, flags: Registry): LegacyCameraEventClaimWorkflow {
    return LegacyCameraEventClaimWorkflow(
        store = store,
        validator = defaultLegacyCameraEventClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyCameraEventClaimEngine(flags),
    )
}
