package com.acme.transport.network.zone.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves fallback zone thresholds through their life cycle and keeps a journal of what it did. */
data class FallbackZoneThresholdDesk(
    var store: FallbackZoneThresholdStore,
    var validator: FallbackZoneThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackZoneThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback zone threshold $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackZoneThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback zone threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackZoneThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback zone threshold $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackZoneThresholdDesk(store: FallbackZoneThresholdStore, clock: Clock, flags: Registry): FallbackZoneThresholdDesk {
    return FallbackZoneThresholdDesk(
        store = store,
        validator = defaultFallbackZoneThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackZoneThresholdEngine(flags),
    )
}
