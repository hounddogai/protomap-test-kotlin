package com.acme.transport.safety.incident.plan

import com.acme.transport.safety.clock.Clock

/** Moves incident templates through their life cycle and keeps a journal of what it did. */
data class IncidentTemplateDesk(
    var store: IncidentTemplateStore,
    var validator: IncidentTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident template $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("incident template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newIncidentTemplateDesk(store: IncidentTemplateStore, clock: Clock): IncidentTemplateDesk {
    return IncidentTemplateDesk(
        store = store,
        validator = defaultIncidentTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
