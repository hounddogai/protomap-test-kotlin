package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves regional zone segments through their life cycle and keeps a journal of what it did. */
data class RegionalZoneSegmentManager(
    var store: RegionalZoneSegmentStore,
    var validator: RegionalZoneSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalZoneSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional zone segment $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalZoneSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional zone segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalZoneSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional zone segment $code not found")
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

fun newRegionalZoneSegmentManager(store: RegionalZoneSegmentStore, clock: Clock, flags: Registry): RegionalZoneSegmentManager {
    return RegionalZoneSegmentManager(
        store = store,
        validator = defaultRegionalZoneSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalZoneSegmentEngine(flags),
    )
}
