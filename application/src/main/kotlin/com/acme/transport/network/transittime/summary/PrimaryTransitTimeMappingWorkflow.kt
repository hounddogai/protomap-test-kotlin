package com.acme.transport.network.transittime.summary

import com.acme.transport.network.clock.Clock

/** Moves primary transit time mappings through their life cycle and keeps a journal of what it did. */
data class PrimaryTransitTimeMappingCoordinator(
    var store: PrimaryTransitTimeMappingStore,
    var validator: PrimaryTransitTimeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary transit time mapping $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTransitTimeMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary transit time mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTransitTimeMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryTransitTimeMappingCoordinator(store: PrimaryTransitTimeMappingStore, clock: Clock): PrimaryTransitTimeMappingCoordinator {
    return PrimaryTransitTimeMappingCoordinator(
        store = store,
        validator = defaultPrimaryTransitTimeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
