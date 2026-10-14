package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted defect manifests through their life cycle and keeps a journal of what it did. */
data class WeightedDefectManifestCoordinator(
    var store: WeightedDefectManifestStore,
    var validator: WeightedDefectManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDefectManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted defect manifest $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDefectManifestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted defect manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDefectManifestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted defect manifest $code not found")
        val result = engine.run(item)
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

fun newWeightedDefectManifestCoordinator(store: WeightedDefectManifestStore, clock: Clock): WeightedDefectManifestCoordinator {
    return WeightedDefectManifestCoordinator(
        store = store,
        validator = defaultWeightedDefectManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDefectManifestEngine(),
    )
}
