package com.acme.transport.linehaul.trailer.group

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer mappings through their life cycle and keeps a journal of what it did. */
data class TrailerMappingCoordinator(
    var store: TrailerMappingStore,
    var validator: TrailerMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer mapping $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTrailerMappingCoordinator(store: TrailerMappingStore, clock: Clock): TrailerMappingCoordinator {
    return TrailerMappingCoordinator(
        store = store,
        validator = defaultTrailerMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
