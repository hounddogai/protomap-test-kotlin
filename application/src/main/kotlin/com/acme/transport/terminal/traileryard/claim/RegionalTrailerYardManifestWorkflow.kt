package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves regional trailer yard manifests through their life cycle and keeps a journal of what it did.
 */
data class RegionalTrailerYardManifestCoordinator(
    var store: RegionalTrailerYardManifestStore,
    var validator: RegionalTrailerYardManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalTrailerYardManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional trailer yard manifest $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTrailerYardManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional trailer yard manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTrailerYardManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional trailer yard manifest $code not found")
        val result = engine.price(item)
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

fun newRegionalTrailerYardManifestCoordinator(store: RegionalTrailerYardManifestStore, clock: Clock, flags: Registry): RegionalTrailerYardManifestCoordinator {
    return RegionalTrailerYardManifestCoordinator(
        store = store,
        validator = defaultRegionalTrailerYardManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalTrailerYardManifestEngine(flags),
    )
}
