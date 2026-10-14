package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy tire digests through their life cycle and keeps a journal of what it did. */
data class LegacyTireDigestService(
    var store: LegacyTireDigestStore,
    var validator: LegacyTireDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTireDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy tire digest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTireDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy tire digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTireDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy tire digest $code not found")
        val result = engine.run(item)
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

fun newLegacyTireDigestService(store: LegacyTireDigestStore, clock: Clock, flags: Registry): LegacyTireDigestService {
    return LegacyTireDigestService(
        store = store,
        validator = defaultLegacyTireDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTireDigestEngine(flags),
    )
}
