package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy tire assignments through their life cycle and keeps a journal of what it did. */
data class LegacyTireAssignmentCoordinator(
    var store: LegacyTireAssignmentStore,
    var validator: LegacyTireAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTireAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy tire assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTireAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy tire assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTireAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy tire assignment $code not found")
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

fun newLegacyTireAssignmentCoordinator(store: LegacyTireAssignmentStore, clock: Clock): LegacyTireAssignmentCoordinator {
    return LegacyTireAssignmentCoordinator(
        store = store,
        validator = defaultLegacyTireAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTireAssignmentEngine(),
    )
}
