package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.clock.Clock

/** Moves primary fuel burn reviews through their life cycle and keeps a journal of what it did. */
data class PrimaryFuelBurnReviewManager(
    var store: PrimaryFuelBurnReviewStore,
    var validator: PrimaryFuelBurnReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary fuel burn review $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelBurnReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel burn review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelBurnReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPrimaryFuelBurnReviewManager(store: PrimaryFuelBurnReviewStore, clock: Clock): PrimaryFuelBurnReviewManager {
    return PrimaryFuelBurnReviewManager(
        store = store,
        validator = defaultPrimaryFuelBurnReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
