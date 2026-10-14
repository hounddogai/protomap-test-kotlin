package com.acme.transport.network.hub.batch

import com.acme.transport.network.clock.Clock

/** Moves strict hub batches through their life cycle and keeps a journal of what it did. */
data class StrictHubBatchService(
    var store: StrictHubBatchStore,
    var validator: StrictHubBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictHubBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict hub batch $code not found")
        validator.requireValid(item)
        item.moveTo(StrictHubBatchStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict hub batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictHubBatchStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict hub batch $code not found")
        val result = engine.compute(item)
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

fun newStrictHubBatchService(store: StrictHubBatchStore, clock: Clock): StrictHubBatchService {
    return StrictHubBatchService(
        store = store,
        validator = defaultStrictHubBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictHubBatchEngine(),
    )
}
