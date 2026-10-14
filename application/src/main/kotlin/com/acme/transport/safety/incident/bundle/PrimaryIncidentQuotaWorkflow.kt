package com.acme.transport.safety.incident.bundle

import com.acme.transport.safety.clock.Clock

/** Moves primary incident quotas through their life cycle and keeps a journal of what it did. */
data class PrimaryIncidentQuotaWorkflow(
    var store: PrimaryIncidentQuotaStore,
    var validator: PrimaryIncidentQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryIncidentQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary incident quota $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryIncidentQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary incident quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryIncidentQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary incident quota $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryIncidentQuotaWorkflow(store: PrimaryIncidentQuotaStore, clock: Clock): PrimaryIncidentQuotaWorkflow {
    return PrimaryIncidentQuotaWorkflow(
        store = store,
        validator = defaultPrimaryIncidentQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryIncidentQuotaEngine(),
    )
}
