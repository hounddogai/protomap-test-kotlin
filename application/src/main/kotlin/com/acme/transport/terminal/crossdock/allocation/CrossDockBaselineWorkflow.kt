package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock baselines through their life cycle and keeps a journal of what it did. */
data class CrossDockBaselineWorkflow(
    var store: CrossDockBaselineStore,
    var validator: CrossDockBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock baseline $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("cross dock baseline $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newCrossDockBaselineWorkflow(store: CrossDockBaselineStore, clock: Clock): CrossDockBaselineWorkflow {
    return CrossDockBaselineWorkflow(
        store = store,
        validator = defaultCrossDockBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockBaselineEngine(),
    )
}
