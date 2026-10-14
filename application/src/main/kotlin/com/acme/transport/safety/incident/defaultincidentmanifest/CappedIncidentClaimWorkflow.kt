package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.safety.clock.Clock

/** Moves capped incident claims through their life cycle and keeps a journal of what it did. */
data class CappedIncidentClaimCoordinator(
    var store: CappedIncidentClaimStore,
    var validator: CappedIncidentClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedIncidentClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped incident claim $code not found")
        validator.requireValid(item)
        item.moveTo(CappedIncidentClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped incident claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedIncidentClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped incident claim $code not found")
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

fun newCappedIncidentClaimCoordinator(store: CappedIncidentClaimStore, clock: Clock): CappedIncidentClaimCoordinator {
    return CappedIncidentClaimCoordinator(
        store = store,
        validator = defaultCappedIncidentClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedIncidentClaimEngine(),
    )
}
