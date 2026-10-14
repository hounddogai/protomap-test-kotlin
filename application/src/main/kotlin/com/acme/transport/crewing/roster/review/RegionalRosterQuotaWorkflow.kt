package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional roster quotas through their life cycle and keeps a journal of what it did. */
data class RegionalRosterQuotaCoordinator(
    var store: RegionalRosterQuotaStore,
    var validator: RegionalRosterQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRosterQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional roster quota $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRosterQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional roster quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRosterQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional roster quota $code not found")
        val result = engine.price(item)
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

fun newRegionalRosterQuotaCoordinator(store: RegionalRosterQuotaStore, clock: Clock, flags: Registry): RegionalRosterQuotaCoordinator {
    return RegionalRosterQuotaCoordinator(
        store = store,
        validator = defaultRegionalRosterQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRosterQuotaEngine(flags),
    )
}
