package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.clock.Clock

/**
 * Moves seasonal emission factor claims through their life cycle and keeps a journal of what it did.
 */
data class SeasonalEmissionFactorClaimService(
    var store: SeasonalEmissionFactorClaimStore,
    var validator: SeasonalEmissionFactorClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal emission factor claim $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEmissionFactorClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal emission factor claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEmissionFactorClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newSeasonalEmissionFactorClaimService(store: SeasonalEmissionFactorClaimStore, clock: Clock): SeasonalEmissionFactorClaimService {
    return SeasonalEmissionFactorClaimService(
        store = store,
        validator = defaultSeasonalEmissionFactorClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
