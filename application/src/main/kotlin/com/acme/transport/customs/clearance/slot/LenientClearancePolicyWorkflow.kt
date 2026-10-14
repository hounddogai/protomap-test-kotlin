package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient clearance policies through their life cycle and keeps a journal of what it did. */
data class LenientClearancePolicyService(
    var store: LenientClearancePolicyStore,
    var validator: LenientClearancePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientClearancePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient clearance policy $code not found")
        validator.requireValid(item)
        item.moveTo(LenientClearancePolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient clearance policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientClearancePolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient clearance policy $code not found")
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

fun newLenientClearancePolicyService(store: LenientClearancePolicyStore, clock: Clock, flags: Registry): LenientClearancePolicyService {
    return LenientClearancePolicyService(
        store = store,
        validator = defaultLenientClearancePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientClearancePolicyEngine(flags),
    )
}
