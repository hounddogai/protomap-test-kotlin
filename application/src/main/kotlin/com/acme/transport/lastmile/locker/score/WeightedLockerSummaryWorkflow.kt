package com.acme.transport.lastmile.locker.score

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted locker summaries through their life cycle and keeps a journal of what it did. */
data class WeightedLockerSummaryCoordinator(
    var store: WeightedLockerSummaryStore,
    var validator: WeightedLockerSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted locker summary $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLockerSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted locker summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLockerSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newWeightedLockerSummaryCoordinator(store: WeightedLockerSummaryStore, clock: Clock): WeightedLockerSummaryCoordinator {
    return WeightedLockerSummaryCoordinator(
        store = store,
        validator = defaultWeightedLockerSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
