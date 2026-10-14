package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves global cross dock preferences through their life cycle and keeps a journal of what it did. */
data class GlobalCrossDockPreferenceWorkflow(
    var store: GlobalCrossDockPreferenceStore,
    var validator: GlobalCrossDockPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCrossDockPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global cross dock preference $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCrossDockPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global cross dock preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCrossDockPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global cross dock preference $code not found")
        val result = engine.price(item)
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

fun newGlobalCrossDockPreferenceWorkflow(store: GlobalCrossDockPreferenceStore, clock: Clock, flags: Registry): GlobalCrossDockPreferenceWorkflow {
    return GlobalCrossDockPreferenceWorkflow(
        store = store,
        validator = defaultGlobalCrossDockPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCrossDockPreferenceEngine(flags),
    )
}
