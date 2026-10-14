package com.acme.transport.safety.incident.variance

import com.acme.transport.safety.clock.Clock

/** Moves legacy incident variances through their life cycle and keeps a journal of what it did. */
data class LegacyIncidentVarianceWorkflow(
    var store: LegacyIncidentVarianceStore,
    var validator: LegacyIncidentVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy incident variance $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyIncidentVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy incident variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyIncidentVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLegacyIncidentVarianceWorkflow(store: LegacyIncidentVarianceStore, clock: Clock): LegacyIncidentVarianceWorkflow {
    return LegacyIncidentVarianceWorkflow(
        store = store,
        validator = defaultLegacyIncidentVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
