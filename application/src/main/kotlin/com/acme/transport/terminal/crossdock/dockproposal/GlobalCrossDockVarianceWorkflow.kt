package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.terminal.clock.Clock

/** Moves global cross dock variances through their life cycle and keeps a journal of what it did. */
data class GlobalCrossDockVarianceWorkflow(
    var store: GlobalCrossDockVarianceStore,
    var validator: GlobalCrossDockVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCrossDockVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global cross dock variance $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCrossDockVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global cross dock variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCrossDockVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global cross dock variance $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newGlobalCrossDockVarianceWorkflow(store: GlobalCrossDockVarianceStore, clock: Clock): GlobalCrossDockVarianceWorkflow {
    return GlobalCrossDockVarianceWorkflow(
        store = store,
        validator = defaultGlobalCrossDockVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCrossDockVarianceEngine(),
    )
}
