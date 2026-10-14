package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.clock.Clock

/** Moves seasonal tariff overrides through their life cycle and keeps a journal of what it did. */
data class SeasonalTariffOverrideService(
    var store: SeasonalTariffOverrideStore,
    var validator: SeasonalTariffOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTariffOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal tariff override $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTariffOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tariff override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTariffOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal tariff override $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalTariffOverrideService(store: SeasonalTariffOverrideStore, clock: Clock): SeasonalTariffOverrideService {
    return SeasonalTariffOverrideService(
        store = store,
        validator = defaultSeasonalTariffOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTariffOverrideEngine(),
    )
}
