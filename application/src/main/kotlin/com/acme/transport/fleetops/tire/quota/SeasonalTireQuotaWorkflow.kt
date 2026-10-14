package com.acme.transport.fleetops.tire.quota

import com.acme.transport.fleetops.clock.Clock

/** Moves seasonal tire quotas through their life cycle and keeps a journal of what it did. */
data class SeasonalTireQuotaService(
    var store: SeasonalTireQuotaStore,
    var validator: SeasonalTireQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTireQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal tire quota $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTireQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tire quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTireQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal tire quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newSeasonalTireQuotaService(store: SeasonalTireQuotaStore, clock: Clock): SeasonalTireQuotaService {
    return SeasonalTireQuotaService(
        store = store,
        validator = defaultSeasonalTireQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTireQuotaEngine(),
    )
}
