package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance policies through their life cycle and keeps a journal of what it did. */
data class ClearancePolicyService(
    var store: ClearancePolicyStore,
    var validator: ClearancePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearancePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("clearance policy $code not found")
        validator.requireValid(item)
        item.moveTo(ClearancePolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearancePolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance policy $code not found")
        val result = engine.compute(item)
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

fun newClearancePolicyService(store: ClearancePolicyStore, clock: Clock, flags: Registry): ClearancePolicyService {
    return ClearancePolicyService(
        store = store,
        validator = defaultClearancePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearancePolicyEngine(flags),
    )
}
