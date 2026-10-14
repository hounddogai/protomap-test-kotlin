package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal inspection drafts through their life cycle and keeps a journal of what it did. */
data class SeasonalInspectionDraftDesk(
    var store: SeasonalInspectionDraftStore,
    var validator: SeasonalInspectionDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalInspectionDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal inspection draft $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalInspectionDraftStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal inspection draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalInspectionDraftStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal inspection draft $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalInspectionDraftDesk(store: SeasonalInspectionDraftStore, clock: Clock, flags: Registry): SeasonalInspectionDraftDesk {
    return SeasonalInspectionDraftDesk(
        store = store,
        validator = defaultSeasonalInspectionDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalInspectionDraftEngine(flags),
    )
}
