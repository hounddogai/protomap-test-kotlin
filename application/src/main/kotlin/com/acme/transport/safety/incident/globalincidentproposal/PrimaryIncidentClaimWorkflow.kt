package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves primary incident claims through their life cycle and keeps a journal of what it did. */
data class PrimaryIncidentClaimManager(
    var store: PrimaryIncidentClaimStore,
    var validator: PrimaryIncidentClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryIncidentClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary incident claim $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryIncidentClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary incident claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryIncidentClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary incident claim $code not found")
        val result = engine.run(item)
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

fun newPrimaryIncidentClaimManager(store: PrimaryIncidentClaimStore, clock: Clock, flags: Registry): PrimaryIncidentClaimManager {
    return PrimaryIncidentClaimManager(
        store = store,
        validator = defaultPrimaryIncidentClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryIncidentClaimEngine(flags),
    )
}
