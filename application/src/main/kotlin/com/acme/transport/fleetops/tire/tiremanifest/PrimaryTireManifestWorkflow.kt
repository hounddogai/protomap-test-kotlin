package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.clock.Clock

/** Moves primary tire manifests through their life cycle and keeps a journal of what it did. */
data class PrimaryTireManifestService(
    var store: PrimaryTireManifestStore,
    var validator: PrimaryTireManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTireManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary tire manifest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTireManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tire manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTireManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary tire manifest $code not found")
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

fun newPrimaryTireManifestService(store: PrimaryTireManifestStore, clock: Clock): PrimaryTireManifestService {
    return PrimaryTireManifestService(
        store = store,
        validator = defaultPrimaryTireManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTireManifestEngine(),
    )
}
