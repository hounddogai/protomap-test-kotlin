package com.acme.transport.lastmile.drop.draft

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal drop audits through their life cycle and keeps a journal of what it did. */
data class SeasonalDropAuditDesk(
    var store: SeasonalDropAuditStore,
    var validator: SeasonalDropAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDropAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal drop audit $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDropAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal drop audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDropAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal drop audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalDropAuditDesk(store: SeasonalDropAuditStore, clock: Clock): SeasonalDropAuditDesk {
    return SeasonalDropAuditDesk(
        store = store,
        validator = defaultSeasonalDropAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDropAuditEngine(),
    )
}
