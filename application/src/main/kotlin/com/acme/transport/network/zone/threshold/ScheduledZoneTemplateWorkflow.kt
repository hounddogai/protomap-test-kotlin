package com.acme.transport.network.zone.threshold

import com.acme.transport.network.clock.Clock

/** Moves scheduled zone templates through their life cycle and keeps a journal of what it did. */
data class ScheduledZoneTemplateDesk(
    var store: ScheduledZoneTemplateStore,
    var validator: ScheduledZoneTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled zone template $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledZoneTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled zone template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledZoneTemplateStatus.STOPPED)
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

fun newScheduledZoneTemplateDesk(store: ScheduledZoneTemplateStore, clock: Clock): ScheduledZoneTemplateDesk {
    return ScheduledZoneTemplateDesk(
        store = store,
        validator = defaultScheduledZoneTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
