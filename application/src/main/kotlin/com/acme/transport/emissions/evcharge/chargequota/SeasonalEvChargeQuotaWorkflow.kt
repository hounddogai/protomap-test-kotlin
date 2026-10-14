package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.clock.Clock

/** Moves seasonal ev charge quotas through their life cycle and keeps a journal of what it did. */
data class SeasonalEvChargeQuotaDesk(
    var store: SeasonalEvChargeQuotaStore,
    var validator: SeasonalEvChargeQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal ev charge quota $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEvChargeQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal ev charge quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEvChargeQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalEvChargeQuotaDesk(store: SeasonalEvChargeQuotaStore, clock: Clock): SeasonalEvChargeQuotaDesk {
    return SeasonalEvChargeQuotaDesk(
        store = store,
        validator = defaultSeasonalEvChargeQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
