package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback qualification milestones through their life cycle and keeps a journal of what it did.
 */
data class FallbackQualificationMilestoneService(
    var store: FallbackQualificationMilestoneStore,
    var validator: FallbackQualificationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackQualificationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback qualification milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackQualificationMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback qualification milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackQualificationMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback qualification milestone $code not found")
        val result = engine.price(item)
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

fun newFallbackQualificationMilestoneService(store: FallbackQualificationMilestoneStore, clock: Clock, flags: Registry): FallbackQualificationMilestoneService {
    return FallbackQualificationMilestoneService(
        store = store,
        validator = defaultFallbackQualificationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackQualificationMilestoneEngine(flags),
    )
}
