package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.clock.Clock

/** Moves scheduled hold templates through their life cycle and keeps a journal of what it did. */
data class ScheduledHoldTemplateManager(
    var store: ScheduledHoldTemplateStore,
    var validator: ScheduledHoldTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled hold template $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledHoldTemplateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled hold template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledHoldTemplateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newScheduledHoldTemplateManager(store: ScheduledHoldTemplateStore, clock: Clock): ScheduledHoldTemplateManager {
    return ScheduledHoldTemplateManager(
        store = store,
        validator = defaultScheduledHoldTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
