package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dwell scores through their life cycle and keeps a journal of what it did. */
data class DwellScoreWorkflow(
    var store: DwellScoreStore,
    var validator: DwellScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell score $code not found")
        validator.requireValid(item)
        item.moveTo(DwellScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dwell score $code not found")
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

fun newDwellScoreWorkflow(store: DwellScoreStore, clock: Clock, flags: Registry): DwellScoreWorkflow {
    return DwellScoreWorkflow(
        store = store,
        validator = defaultDwellScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellScoreEngine(flags),
    )
}
