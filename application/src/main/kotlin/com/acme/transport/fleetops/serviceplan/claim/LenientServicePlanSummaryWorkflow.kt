package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves lenient service plan summaries through their life cycle and keeps a journal of what it did.
 */
data class LenientServicePlanSummaryService(
    var store: LenientServicePlanSummaryStore,
    var validator: LenientServicePlanSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient service plan summary $code not found")
        validator.requireValid(item)
        item.moveTo(LenientServicePlanSummaryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient service plan summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientServicePlanSummaryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newLenientServicePlanSummaryService(store: LenientServicePlanSummaryStore, clock: Clock): LenientServicePlanSummaryService {
    return LenientServicePlanSummaryService(
        store = store,
        validator = defaultLenientServicePlanSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
