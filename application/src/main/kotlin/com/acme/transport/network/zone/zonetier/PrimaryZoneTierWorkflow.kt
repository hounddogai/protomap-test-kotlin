package com.acme.transport.network.zone.zonetier

import com.acme.transport.network.clock.Clock

/** Moves primary zone tiers through their life cycle and keeps a journal of what it did. */
data class PrimaryZoneTierService(
    var store: PrimaryZoneTierStore,
    var validator: PrimaryZoneTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary zone tier $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryZoneTierStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary zone tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryZoneTierStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryZoneTierService(store: PrimaryZoneTierStore, clock: Clock): PrimaryZoneTierService {
    return PrimaryZoneTierService(
        store = store,
        validator = defaultPrimaryZoneTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
