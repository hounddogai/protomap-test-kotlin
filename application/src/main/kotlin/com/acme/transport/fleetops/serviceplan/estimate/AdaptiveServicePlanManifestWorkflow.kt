package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive service plan manifests through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveServicePlanManifestDesk(
    var store: AdaptiveServicePlanManifestStore,
    var validator: AdaptiveServicePlanManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveServicePlanManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive service plan manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveServicePlanManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive service plan manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveServicePlanManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive service plan manifest $code not found")
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

fun newAdaptiveServicePlanManifestDesk(store: AdaptiveServicePlanManifestStore, clock: Clock, flags: Registry): AdaptiveServicePlanManifestDesk {
    return AdaptiveServicePlanManifestDesk(
        store = store,
        validator = defaultAdaptiveServicePlanManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveServicePlanManifestEngine(flags),
    )
}
