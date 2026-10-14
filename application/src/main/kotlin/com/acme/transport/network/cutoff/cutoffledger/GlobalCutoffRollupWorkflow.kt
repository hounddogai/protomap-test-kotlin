package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global cutoff rollups through their life cycle and keeps a journal of what it did. */
data class GlobalCutoffRollupManager(
    var store: GlobalCutoffRollupStore,
    var validator: GlobalCutoffRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCutoffRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global cutoff rollup $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCutoffRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global cutoff rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCutoffRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global cutoff rollup $code not found")
        val result = engine.evaluate(item)
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

fun newGlobalCutoffRollupManager(store: GlobalCutoffRollupStore, clock: Clock, flags: Registry): GlobalCutoffRollupManager {
    return GlobalCutoffRollupManager(
        store = store,
        validator = defaultGlobalCutoffRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCutoffRollupEngine(flags),
    )
}
