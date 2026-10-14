package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn reviews through their life cycle and keeps a journal of what it did. */
data class FuelBurnReviewService(
    var store: FuelBurnReviewStore,
    var validator: FuelBurnReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel burn review $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newFuelBurnReviewService(store: FuelBurnReviewStore, clock: Clock): FuelBurnReviewService {
    return FuelBurnReviewService(
        store = store,
        validator = defaultFuelBurnReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
