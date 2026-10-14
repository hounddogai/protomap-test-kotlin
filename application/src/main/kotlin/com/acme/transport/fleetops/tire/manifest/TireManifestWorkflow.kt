package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire manifests through their life cycle and keeps a journal of what it did. */
data class TireManifestManager(
    var store: TireManifestStore,
    var validator: TireManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tire manifest $code not found")
        validator.requireValid(item)
        item.moveTo(TireManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tire manifest $code not found")
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

fun newTireManifestManager(store: TireManifestStore, clock: Clock, flags: Registry): TireManifestManager {
    return TireManifestManager(
        store = store,
        validator = defaultTireManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireManifestEngine(flags),
    )
}
