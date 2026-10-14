package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves batched qualification summaries through their life cycle and keeps a journal of what it did.
 */
data class BatchedQualificationSummaryService(
    var store: BatchedQualificationSummaryStore,
    var validator: BatchedQualificationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedQualificationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched qualification summary $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedQualificationSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched qualification summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedQualificationSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched qualification summary $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newBatchedQualificationSummaryService(store: BatchedQualificationSummaryStore, clock: Clock, flags: Registry): BatchedQualificationSummaryService {
    return BatchedQualificationSummaryService(
        store = store,
        validator = defaultBatchedQualificationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedQualificationSummaryEngine(flags),
    )
}
