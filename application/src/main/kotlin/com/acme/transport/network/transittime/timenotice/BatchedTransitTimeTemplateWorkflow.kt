package com.acme.transport.network.transittime.timenotice

import com.acme.transport.network.clock.Clock

/**
 * Moves batched transit time templates through their life cycle and keeps a journal of what it did.
 */
data class BatchedTransitTimeTemplateService(
    var store: BatchedTransitTimeTemplateStore,
    var validator: BatchedTransitTimeTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTransitTimeTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched transit time template $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTransitTimeTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched transit time template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTransitTimeTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched transit time template $code not found")
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

fun newBatchedTransitTimeTemplateService(store: BatchedTransitTimeTemplateStore, clock: Clock): BatchedTransitTimeTemplateService {
    return BatchedTransitTimeTemplateService(
        store = store,
        validator = defaultBatchedTransitTimeTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTransitTimeTemplateEngine(),
    )
}
