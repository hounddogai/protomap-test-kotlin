package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.linehaul.clock.Clock

/** Moves strict lane plan mappings through their life cycle and keeps a journal of what it did. */
data class StrictLanePlanMappingManager(
    var store: StrictLanePlanMappingStore,
    var validator: StrictLanePlanMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict lane plan mapping $code not found")
        validator.requireValid(item)
        item.moveTo(StrictLanePlanMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict lane plan mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictLanePlanMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictLanePlanMappingManager(store: StrictLanePlanMappingStore, clock: Clock): StrictLanePlanMappingManager {
    return StrictLanePlanMappingManager(
        store = store,
        validator = defaultStrictLanePlanMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
