package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive tire claims through their life cycle and keeps a journal of what it did. */
data class AdaptiveTireClaimWorkflow(
    var store: AdaptiveTireClaimStore,
    var validator: AdaptiveTireClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveTireClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive tire claim $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTireClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive tire claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTireClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive tire claim $code not found")
        val result = engine.compute(item)
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

fun newAdaptiveTireClaimWorkflow(store: AdaptiveTireClaimStore, clock: Clock): AdaptiveTireClaimWorkflow {
    return AdaptiveTireClaimWorkflow(
        store = store,
        validator = defaultAdaptiveTireClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveTireClaimEngine(),
    )
}
