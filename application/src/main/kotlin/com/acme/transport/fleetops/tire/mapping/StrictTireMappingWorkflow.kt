package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.clock.Clock

/** Moves strict tire mappings through their life cycle and keeps a journal of what it did. */
data class StrictTireMappingCoordinator(
    var store: StrictTireMappingStore,
    var validator: StrictTireMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict tire mapping $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTireMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict tire mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTireMappingStatus.RETIRED)
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

fun newStrictTireMappingCoordinator(store: StrictTireMappingStore, clock: Clock): StrictTireMappingCoordinator {
    return StrictTireMappingCoordinator(
        store = store,
        validator = defaultStrictTireMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
