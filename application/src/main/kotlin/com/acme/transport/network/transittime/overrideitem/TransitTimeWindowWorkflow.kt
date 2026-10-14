package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.network.clock.Clock

/** Moves transit time windows through their life cycle and keeps a journal of what it did. */
data class TransitTimeWindowDesk(
    var store: TransitTimeWindowStore,
    var validator: TransitTimeWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("transit time window $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeWindowStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeWindowStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newTransitTimeWindowDesk(store: TransitTimeWindowStore, clock: Clock): TransitTimeWindowDesk {
    return TransitTimeWindowDesk(
        store = store,
        validator = defaultTransitTimeWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
