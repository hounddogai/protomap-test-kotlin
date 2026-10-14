package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay cycles through their life cycle and keeps a journal of what it did. */
data class RelayCycleWorkflow(
    var store: RelayCycleStore,
    var validator: RelayCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("relay cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RelayCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("relay cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay cycle $code not found")
        val result = engine.run(item)
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

fun newRelayCycleWorkflow(store: RelayCycleStore, clock: Clock, flags: Registry): RelayCycleWorkflow {
    return RelayCycleWorkflow(
        store = store,
        validator = defaultRelayCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayCycleEngine(flags),
    )
}
