package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.Clock

/** Moves capped fuel burn manifests through their life cycle and keeps a journal of what it did. */
data class CappedFuelBurnManifestManager(
    var store: CappedFuelBurnManifestStore,
    var validator: CappedFuelBurnManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedFuelBurnManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped fuel burn manifest $code not found")
        validator.requireValid(item)
        item.moveTo(CappedFuelBurnManifestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped fuel burn manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedFuelBurnManifestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped fuel burn manifest $code not found")
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

fun newCappedFuelBurnManifestManager(store: CappedFuelBurnManifestStore, clock: Clock): CappedFuelBurnManifestManager {
    return CappedFuelBurnManifestManager(
        store = store,
        validator = defaultCappedFuelBurnManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedFuelBurnManifestEngine(),
    )
}
