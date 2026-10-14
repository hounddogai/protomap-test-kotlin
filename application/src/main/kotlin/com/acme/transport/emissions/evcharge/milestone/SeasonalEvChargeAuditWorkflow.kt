package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.emissions.clock.Clock

/** Moves seasonal ev charge audits through their life cycle and keeps a journal of what it did. */
data class SeasonalEvChargeAuditService(
    var store: SeasonalEvChargeAuditStore,
    var validator: SeasonalEvChargeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal ev charge audit $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEvChargeAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal ev charge audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEvChargeAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newSeasonalEvChargeAuditService(store: SeasonalEvChargeAuditStore, clock: Clock): SeasonalEvChargeAuditService {
    return SeasonalEvChargeAuditService(
        store = store,
        validator = defaultSeasonalEvChargeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
