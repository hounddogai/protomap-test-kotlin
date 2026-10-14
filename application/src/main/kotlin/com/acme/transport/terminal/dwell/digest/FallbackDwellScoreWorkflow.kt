package com.acme.transport.terminal.dwell.digest

import com.acme.transport.terminal.clock.Clock

/** Moves fallback dwell scores through their life cycle and keeps a journal of what it did. */
data class FallbackDwellScoreWorkflow(
    var store: FallbackDwellScoreStore,
    var validator: FallbackDwellScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDwellScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback dwell score $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDwellScoreStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dwell score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDwellScoreStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback dwell score $code not found")
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

fun newFallbackDwellScoreWorkflow(store: FallbackDwellScoreStore, clock: Clock): FallbackDwellScoreWorkflow {
    return FallbackDwellScoreWorkflow(
        store = store,
        validator = defaultFallbackDwellScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDwellScoreEngine(),
    )
}
