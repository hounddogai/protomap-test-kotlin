package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive route offset manifests through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveRouteOffsetManifestWorkflow(
    var store: AdaptiveRouteOffsetManifestStore,
    var validator: AdaptiveRouteOffsetManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveRouteOffsetManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive route offset manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRouteOffsetManifestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive route offset manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRouteOffsetManifestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive route offset manifest $code not found")
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

fun newAdaptiveRouteOffsetManifestWorkflow(store: AdaptiveRouteOffsetManifestStore, clock: Clock): AdaptiveRouteOffsetManifestWorkflow {
    return AdaptiveRouteOffsetManifestWorkflow(
        store = store,
        validator = defaultAdaptiveRouteOffsetManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveRouteOffsetManifestEngine(),
    )
}
