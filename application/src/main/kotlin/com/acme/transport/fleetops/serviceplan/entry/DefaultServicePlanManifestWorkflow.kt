package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves default service plan manifests through their life cycle and keeps a journal of what it did.
 */
data class DefaultServicePlanManifestDesk(
    var store: DefaultServicePlanManifestStore,
    var validator: DefaultServicePlanManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultServicePlanManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default service plan manifest $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServicePlanManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default service plan manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServicePlanManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default service plan manifest $code not found")
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

fun newDefaultServicePlanManifestDesk(store: DefaultServicePlanManifestStore, clock: Clock, flags: Registry): DefaultServicePlanManifestDesk {
    return DefaultServicePlanManifestDesk(
        store = store,
        validator = defaultDefaultServicePlanManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultServicePlanManifestEngine(flags),
    )
}
