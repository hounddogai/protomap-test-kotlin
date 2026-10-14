package com.acme.transport.network.zone.snapshot

import com.acme.transport.network.clock.Clock

/** Moves zone targets through their life cycle and keeps a journal of what it did. */
data class ZoneTargetManager(
    var store: ZoneTargetStore,
    var validator: ZoneTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone target $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneTargetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("zone target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneTargetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("zone target $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newZoneTargetManager(store: ZoneTargetStore, clock: Clock): ZoneTargetManager {
    return ZoneTargetManager(
        store = store,
        validator = defaultZoneTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneTargetEngine(),
    )
}
