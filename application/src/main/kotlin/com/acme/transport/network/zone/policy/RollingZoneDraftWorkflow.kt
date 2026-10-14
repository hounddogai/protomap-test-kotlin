package com.acme.transport.network.zone.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves rolling zone drafts through their life cycle and keeps a journal of what it did. */
data class RollingZoneDraftManager(
    var store: RollingZoneDraftStore,
    var validator: RollingZoneDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingZoneDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling zone draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingZoneDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling zone draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingZoneDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling zone draft $code not found")
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

fun newRollingZoneDraftManager(store: RollingZoneDraftStore, clock: Clock, flags: Registry): RollingZoneDraftManager {
    return RollingZoneDraftManager(
        store = store,
        validator = defaultRollingZoneDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingZoneDraftEngine(flags),
    )
}
