package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.Clock

/** Moves seasonal roster templates through their life cycle and keeps a journal of what it did. */
data class SeasonalRosterTemplateWorkflow(
    var store: SeasonalRosterTemplateStore,
    var validator: SeasonalRosterTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal roster template $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRosterTemplateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal roster template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRosterTemplateStatus.EXPIRED)
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

fun newSeasonalRosterTemplateWorkflow(store: SeasonalRosterTemplateStore, clock: Clock): SeasonalRosterTemplateWorkflow {
    return SeasonalRosterTemplateWorkflow(
        store = store,
        validator = defaultSeasonalRosterTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
