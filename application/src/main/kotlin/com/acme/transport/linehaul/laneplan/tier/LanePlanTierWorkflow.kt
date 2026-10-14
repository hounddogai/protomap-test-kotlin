package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan tiers through their life cycle and keeps a journal of what it did. */
data class LanePlanTierCoordinator(
    var store: LanePlanTierStore,
    var validator: LanePlanTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan tier $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanTierStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanTierStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lane plan tier $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newLanePlanTierCoordinator(store: LanePlanTierStore, clock: Clock): LanePlanTierCoordinator {
    return LanePlanTierCoordinator(
        store = store,
        validator = defaultLanePlanTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanTierEngine(),
    )
}
