package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive route offset milestones through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveRouteOffsetMilestoneCoordinator(
    var store: AdaptiveRouteOffsetMilestoneStore,
    var validator: AdaptiveRouteOffsetMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveRouteOffsetMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive route offset milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRouteOffsetMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive route offset milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRouteOffsetMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive route offset milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveRouteOffsetMilestoneCoordinator(store: AdaptiveRouteOffsetMilestoneStore, clock: Clock): AdaptiveRouteOffsetMilestoneCoordinator {
    return AdaptiveRouteOffsetMilestoneCoordinator(
        store = store,
        validator = defaultAdaptiveRouteOffsetMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveRouteOffsetMilestoneEngine(),
    )
}
