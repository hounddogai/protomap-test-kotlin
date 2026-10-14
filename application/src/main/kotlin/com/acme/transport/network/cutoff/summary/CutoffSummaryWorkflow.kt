package com.acme.transport.network.cutoff.summary

import com.acme.transport.network.clock.Clock

/** Moves cutoff summaries through their life cycle and keeps a journal of what it did. */
data class CutoffSummaryManager(
    var store: CutoffSummaryStore,
    var validator: CutoffSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cutoff summary $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("cutoff summary $code not found")
        val result = engine.price(item)
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

fun newCutoffSummaryManager(store: CutoffSummaryStore, clock: Clock): CutoffSummaryManager {
    return CutoffSummaryManager(
        store = store,
        validator = defaultCutoffSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffSummaryEngine(),
    )
}
