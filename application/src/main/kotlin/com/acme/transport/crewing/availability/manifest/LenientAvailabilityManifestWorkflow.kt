package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves lenient availability manifests through their life cycle and keeps a journal of what it did.
 */
data class LenientAvailabilityManifestManager(
    var store: LenientAvailabilityManifestStore,
    var validator: LenientAvailabilityManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAvailabilityManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient availability manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAvailabilityManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient availability manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAvailabilityManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient availability manifest $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newLenientAvailabilityManifestManager(store: LenientAvailabilityManifestStore, clock: Clock, flags: Registry): LenientAvailabilityManifestManager {
    return LenientAvailabilityManifestManager(
        store = store,
        validator = defaultLenientAvailabilityManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAvailabilityManifestEngine(flags),
    )
}
