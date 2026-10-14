package com.acme.transport.network.zone.profile

import com.acme.transport.network.clock.Clock

/** Moves zone requests through their life cycle and keeps a journal of what it did. */
data class ZoneRequestService(
    var store: ZoneRequestStore,
    var validator: ZoneRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone request $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneRequestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("zone request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneRequestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newZoneRequestService(store: ZoneRequestStore, clock: Clock): ZoneRequestService {
    return ZoneRequestService(
        store = store,
        validator = defaultZoneRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
