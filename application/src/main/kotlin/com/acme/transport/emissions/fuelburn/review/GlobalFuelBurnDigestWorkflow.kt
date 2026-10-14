package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global fuel burn digests through their life cycle and keeps a journal of what it did. */
data class GlobalFuelBurnDigestWorkflow(
    var store: GlobalFuelBurnDigestStore,
    var validator: GlobalFuelBurnDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalFuelBurnDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global fuel burn digest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFuelBurnDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global fuel burn digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFuelBurnDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global fuel burn digest $code not found")
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

fun newGlobalFuelBurnDigestWorkflow(store: GlobalFuelBurnDigestStore, clock: Clock, flags: Registry): GlobalFuelBurnDigestWorkflow {
    return GlobalFuelBurnDigestWorkflow(
        store = store,
        validator = defaultGlobalFuelBurnDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalFuelBurnDigestEngine(flags),
    )
}
