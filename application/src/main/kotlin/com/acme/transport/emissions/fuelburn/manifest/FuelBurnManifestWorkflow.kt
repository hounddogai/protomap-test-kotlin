package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn manifests through their life cycle and keeps a journal of what it did. */
data class FuelBurnManifestDesk(
    var store: FuelBurnManifestStore,
    var validator: FuelBurnManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel burn manifest $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel burn manifest $code not found")
        val result = engine.compute(item)
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

fun newFuelBurnManifestDesk(store: FuelBurnManifestStore, clock: Clock): FuelBurnManifestDesk {
    return FuelBurnManifestDesk(
        store = store,
        validator = defaultFuelBurnManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnManifestEngine(),
    )
}
