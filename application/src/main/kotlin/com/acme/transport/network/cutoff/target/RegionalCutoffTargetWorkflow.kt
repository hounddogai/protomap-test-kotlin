package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves regional cutoff targets through their life cycle and keeps a journal of what it did. */
data class RegionalCutoffTargetWorkflow(
    var store: RegionalCutoffTargetStore,
    var validator: RegionalCutoffTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCutoffTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional cutoff target $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCutoffTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional cutoff target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCutoffTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional cutoff target $code not found")
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

fun newRegionalCutoffTargetWorkflow(store: RegionalCutoffTargetStore, clock: Clock, flags: Registry): RegionalCutoffTargetWorkflow {
    return RegionalCutoffTargetWorkflow(
        store = store,
        validator = defaultRegionalCutoffTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCutoffTargetEngine(flags),
    )
}
