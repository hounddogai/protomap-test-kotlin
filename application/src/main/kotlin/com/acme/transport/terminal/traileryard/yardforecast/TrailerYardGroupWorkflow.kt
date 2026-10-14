package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard groups through their life cycle and keeps a journal of what it did. */
data class TrailerYardGroupService(
    var store: TrailerYardGroupStore,
    var validator: TrailerYardGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer yard group $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTrailerYardGroupService(store: TrailerYardGroupStore, clock: Clock): TrailerYardGroupService {
    return TrailerYardGroupService(
        store = store,
        validator = defaultTrailerYardGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
