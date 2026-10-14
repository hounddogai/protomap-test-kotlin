package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.clock.Clock

/**
 * Moves default qualification summaries through their life cycle and keeps a journal of what it did.
 */
data class DefaultQualificationSummaryManager(
    var store: DefaultQualificationSummaryStore,
    var validator: DefaultQualificationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default qualification summary $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultQualificationSummaryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default qualification summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultQualificationSummaryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDefaultQualificationSummaryManager(store: DefaultQualificationSummaryStore, clock: Clock): DefaultQualificationSummaryManager {
    return DefaultQualificationSummaryManager(
        store = store,
        validator = defaultDefaultQualificationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
