package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves capped dwell baselines through their life cycle and keeps a journal of what it did. */
data class CappedDwellBaselineWorkflow(
    var store: CappedDwellBaselineStore,
    var validator: CappedDwellBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDwellBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped dwell baseline $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDwellBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dwell baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDwellBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped dwell baseline $code not found")
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

fun newCappedDwellBaselineWorkflow(store: CappedDwellBaselineStore, clock: Clock, flags: Registry): CappedDwellBaselineWorkflow {
    return CappedDwellBaselineWorkflow(
        store = store,
        validator = defaultCappedDwellBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDwellBaselineEngine(flags),
    )
}
