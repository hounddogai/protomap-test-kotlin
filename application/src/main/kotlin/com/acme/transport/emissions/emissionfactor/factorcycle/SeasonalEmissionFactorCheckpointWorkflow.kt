package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.clock.Clock

/**
 * Moves seasonal emission factor checkpoints through their life cycle and keeps a journal of what it did.
 */
data class SeasonalEmissionFactorCheckpointService(
    var store: SeasonalEmissionFactorCheckpointStore,
    var validator: SeasonalEmissionFactorCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal emission factor checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEmissionFactorCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal emission factor checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEmissionFactorCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newSeasonalEmissionFactorCheckpointService(store: SeasonalEmissionFactorCheckpointStore, clock: Clock): SeasonalEmissionFactorCheckpointService {
    return SeasonalEmissionFactorCheckpointService(
        store = store,
        validator = defaultSeasonalEmissionFactorCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
