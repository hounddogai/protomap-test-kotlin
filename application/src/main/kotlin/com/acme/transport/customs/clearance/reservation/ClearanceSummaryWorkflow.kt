package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.clock.Clock

/** Moves clearance summaries through their life cycle and keeps a journal of what it did. */
data class ClearanceSummaryManager(
    var store: ClearanceSummaryStore,
    var validator: ClearanceSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance summary $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance summary $code not found")
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

fun newClearanceSummaryManager(store: ClearanceSummaryStore, clock: Clock): ClearanceSummaryManager {
    return ClearanceSummaryManager(
        store = store,
        validator = defaultClearanceSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceSummaryEngine(),
    )
}
