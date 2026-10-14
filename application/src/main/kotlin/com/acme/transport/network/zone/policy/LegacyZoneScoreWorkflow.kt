package com.acme.transport.network.zone.policy

import com.acme.transport.network.clock.Clock

/** Moves legacy zone scores through their life cycle and keeps a journal of what it did. */
data class LegacyZoneScoreWorkflow(
    var store: LegacyZoneScoreStore,
    var validator: LegacyZoneScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy zone score $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyZoneScoreStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy zone score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyZoneScoreStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newLegacyZoneScoreWorkflow(store: LegacyZoneScoreStore, clock: Clock): LegacyZoneScoreWorkflow {
    return LegacyZoneScoreWorkflow(
        store = store,
        validator = defaultLegacyZoneScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
