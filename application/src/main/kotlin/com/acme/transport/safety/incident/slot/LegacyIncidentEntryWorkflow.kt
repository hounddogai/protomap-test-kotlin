package com.acme.transport.safety.incident.slot

import com.acme.transport.safety.clock.Clock

/** Moves legacy incident entries through their life cycle and keeps a journal of what it did. */
data class LegacyIncidentEntryManager(
    var store: LegacyIncidentEntryStore,
    var validator: LegacyIncidentEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyIncidentEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy incident entry $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyIncidentEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy incident entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyIncidentEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy incident entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyIncidentEntryManager(store: LegacyIncidentEntryStore, clock: Clock): LegacyIncidentEntryManager {
    return LegacyIncidentEntryManager(
        store = store,
        validator = defaultLegacyIncidentEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyIncidentEntryEngine(),
    )
}
