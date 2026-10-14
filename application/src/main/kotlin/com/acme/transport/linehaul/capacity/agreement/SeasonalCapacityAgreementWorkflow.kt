package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal capacity agreements through their life cycle and keeps a journal of what it did. */
data class SeasonalCapacityAgreementManager(
    var store: SeasonalCapacityAgreementStore,
    var validator: SeasonalCapacityAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCapacityAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal capacity agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCapacityAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal capacity agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCapacityAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal capacity agreement $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newSeasonalCapacityAgreementManager(store: SeasonalCapacityAgreementStore, clock: Clock): SeasonalCapacityAgreementManager {
    return SeasonalCapacityAgreementManager(
        store = store,
        validator = defaultSeasonalCapacityAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCapacityAgreementEngine(),
    )
}
