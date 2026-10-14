package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance cycles through their life cycle and keeps a journal of what it did. */
data class ClearanceCycleWorkflow(
    var store: ClearanceCycleStore,
    var validator: ClearanceCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("clearance cycle $code not found")
        val result = engine.price(item)
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

fun newClearanceCycleWorkflow(store: ClearanceCycleStore, clock: Clock, flags: Registry): ClearanceCycleWorkflow {
    return ClearanceCycleWorkflow(
        store = store,
        validator = defaultClearanceCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceCycleEngine(flags),
    )
}
