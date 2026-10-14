package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.terminal.clock.Clock

/** Moves dwell summaries through their life cycle and keeps a journal of what it did. */
data class DwellSummaryWorkflow(
    var store: DwellSummaryStore,
    var validator: DwellSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dwell summary $code not found")
        validator.requireValid(item)
        item.moveTo(DwellSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDwellSummaryWorkflow(store: DwellSummaryStore, clock: Clock): DwellSummaryWorkflow {
    return DwellSummaryWorkflow(
        store = store,
        validator = defaultDwellSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
