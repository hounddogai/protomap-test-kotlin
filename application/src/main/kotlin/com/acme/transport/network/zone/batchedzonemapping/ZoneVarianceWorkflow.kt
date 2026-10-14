package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves zone variances through their life cycle and keeps a journal of what it did. */
data class ZoneVarianceService(
    var store: ZoneVarianceStore,
    var validator: ZoneVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("zone variance $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("zone variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone variance $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newZoneVarianceService(store: ZoneVarianceStore, clock: Clock, flags: Registry): ZoneVarianceService {
    return ZoneVarianceService(
        store = store,
        validator = defaultZoneVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneVarianceEngine(flags),
    )
}
