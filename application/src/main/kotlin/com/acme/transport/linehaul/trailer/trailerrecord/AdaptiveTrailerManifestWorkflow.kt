package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive trailer manifests through their life cycle and keeps a journal of what it did. */
data class AdaptiveTrailerManifestCoordinator(
    var store: AdaptiveTrailerManifestStore,
    var validator: AdaptiveTrailerManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveTrailerManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive trailer manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTrailerManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive trailer manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTrailerManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive trailer manifest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveTrailerManifestCoordinator(store: AdaptiveTrailerManifestStore, clock: Clock, flags: Registry): AdaptiveTrailerManifestCoordinator {
    return AdaptiveTrailerManifestCoordinator(
        store = store,
        validator = defaultAdaptiveTrailerManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveTrailerManifestEngine(flags),
    )
}
