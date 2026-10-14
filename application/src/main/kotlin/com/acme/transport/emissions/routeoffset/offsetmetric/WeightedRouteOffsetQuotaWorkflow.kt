package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.Clock

/** Moves weighted route offset quotas through their life cycle and keeps a journal of what it did. */
data class WeightedRouteOffsetQuotaDesk(
    var store: WeightedRouteOffsetQuotaStore,
    var validator: WeightedRouteOffsetQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted route offset quota $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRouteOffsetQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted route offset quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRouteOffsetQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newWeightedRouteOffsetQuotaDesk(store: WeightedRouteOffsetQuotaStore, clock: Clock): WeightedRouteOffsetQuotaDesk {
    return WeightedRouteOffsetQuotaDesk(
        store = store,
        validator = defaultWeightedRouteOffsetQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
