package com.acme.transport.customs.clearance.manifest

import com.acme.transport.customs.clock.Clock

/** Moves clearance manifests through their life cycle and keeps a journal of what it did. */
data class ClearanceManifestManager(
    var store: ClearanceManifestStore,
    var validator: ClearanceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance manifest $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance manifest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newClearanceManifestManager(store: ClearanceManifestStore, clock: Clock): ClearanceManifestManager {
    return ClearanceManifestManager(
        store = store,
        validator = defaultClearanceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceManifestEngine(),
    )
}
