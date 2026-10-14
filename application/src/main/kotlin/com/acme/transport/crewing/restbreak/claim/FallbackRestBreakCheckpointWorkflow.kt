package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.clock.Clock

/**
 * Moves fallback rest break checkpoints through their life cycle and keeps a journal of what it did.
 */
data class FallbackRestBreakCheckpointWorkflow(
    var store: FallbackRestBreakCheckpointStore,
    var validator: FallbackRestBreakCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRestBreakCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback rest break checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRestBreakCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rest break checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRestBreakCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback rest break checkpoint $code not found")
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

fun newFallbackRestBreakCheckpointWorkflow(store: FallbackRestBreakCheckpointStore, clock: Clock): FallbackRestBreakCheckpointWorkflow {
    return FallbackRestBreakCheckpointWorkflow(
        store = store,
        validator = defaultFallbackRestBreakCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRestBreakCheckpointEngine(),
    )
}
