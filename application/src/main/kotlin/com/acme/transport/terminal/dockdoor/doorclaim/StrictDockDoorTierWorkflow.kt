package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.terminal.clock.Clock

/** Moves strict dock door tiers through their life cycle and keeps a journal of what it did. */
data class StrictDockDoorTierService(
    var store: StrictDockDoorTierStore,
    var validator: StrictDockDoorTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict dock door tier $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDockDoorTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dock door tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDockDoorTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newStrictDockDoorTierService(store: StrictDockDoorTierStore, clock: Clock): StrictDockDoorTierService {
    return StrictDockDoorTierService(
        store = store,
        validator = defaultStrictDockDoorTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
