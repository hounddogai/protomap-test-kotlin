package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered utilization digests through their life cycle and keeps a journal of what it did. */
data class TieredUtilizationDigestService(
    var store: TieredUtilizationDigestStore,
    var validator: TieredUtilizationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredUtilizationDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered utilization digest $code not found")
        validator.requireValid(item)
        item.moveTo(TieredUtilizationDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered utilization digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredUtilizationDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered utilization digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTieredUtilizationDigestService(store: TieredUtilizationDigestStore, clock: Clock, flags: Registry): TieredUtilizationDigestService {
    return TieredUtilizationDigestService(
        store = store,
        validator = defaultTieredUtilizationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredUtilizationDigestEngine(flags),
    )
}
