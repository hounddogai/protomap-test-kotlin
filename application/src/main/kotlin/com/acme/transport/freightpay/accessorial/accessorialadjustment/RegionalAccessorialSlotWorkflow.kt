package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves regional accessorial slots through their life cycle and keeps a journal of what it did. */
data class RegionalAccessorialSlotCoordinator(
    var store: RegionalAccessorialSlotStore,
    var validator: RegionalAccessorialSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional accessorial slot $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalAccessorialSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional accessorial slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalAccessorialSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRegionalAccessorialSlotCoordinator(store: RegionalAccessorialSlotStore, clock: Clock): RegionalAccessorialSlotCoordinator {
    return RegionalAccessorialSlotCoordinator(
        store = store,
        validator = defaultRegionalAccessorialSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
