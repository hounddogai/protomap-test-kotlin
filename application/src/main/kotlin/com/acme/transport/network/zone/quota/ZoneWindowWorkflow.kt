package com.acme.transport.network.zone.quota

import com.acme.transport.network.clock.Clock

/** Moves zone windows through their life cycle and keeps a journal of what it did. */
data class ZoneWindowManager(
    var store: ZoneWindowStore,
    var validator: ZoneWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone window $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("zone window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone window $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newZoneWindowManager(store: ZoneWindowStore, clock: Clock): ZoneWindowManager {
    return ZoneWindowManager(
        store = store,
        validator = defaultZoneWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneWindowEngine(),
    )
}
