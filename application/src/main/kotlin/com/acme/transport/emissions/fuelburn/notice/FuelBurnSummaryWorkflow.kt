package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn summaries through their life cycle and keeps a journal of what it did. */
data class FuelBurnSummaryDesk(
    var store: FuelBurnSummaryStore,
    var validator: FuelBurnSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel burn summary $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnSummaryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnSummaryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFuelBurnSummaryDesk(store: FuelBurnSummaryStore, clock: Clock): FuelBurnSummaryDesk {
    return FuelBurnSummaryDesk(
        store = store,
        validator = defaultFuelBurnSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
