package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves seasonal hub manifests through their life cycle and keeps a journal of what it did. */
data class SeasonalHubManifestService(
    var store: SeasonalHubManifestStore,
    var validator: SeasonalHubManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalHubManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal hub manifest $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalHubManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal hub manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalHubManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal hub manifest $code not found")
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

fun newSeasonalHubManifestService(store: SeasonalHubManifestStore, clock: Clock, flags: Registry): SeasonalHubManifestService {
    return SeasonalHubManifestService(
        store = store,
        validator = defaultSeasonalHubManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalHubManifestEngine(flags),
    )
}
