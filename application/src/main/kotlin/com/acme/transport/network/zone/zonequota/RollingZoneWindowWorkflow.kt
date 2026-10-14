package com.acme.transport.network.zone.zonequota

import com.acme.transport.network.clock.Clock

/** Moves rolling zone windows through their life cycle and keeps a journal of what it did. */
data class RollingZoneWindowCoordinator(
    var store: RollingZoneWindowStore,
    var validator: RollingZoneWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling zone window $code not found")
        validator.requireValid(item)
        item.moveTo(RollingZoneWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling zone window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingZoneWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRollingZoneWindowCoordinator(store: RollingZoneWindowStore, clock: Clock): RollingZoneWindowCoordinator {
    return RollingZoneWindowCoordinator(
        store = store,
        validator = defaultRollingZoneWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
