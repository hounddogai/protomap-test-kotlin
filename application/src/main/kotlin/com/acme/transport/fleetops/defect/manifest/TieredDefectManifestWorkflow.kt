package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered defect manifests through their life cycle and keeps a journal of what it did. */
data class TieredDefectManifestDesk(
    var store: TieredDefectManifestStore,
    var validator: TieredDefectManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDefectManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered defect manifest $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDefectManifestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered defect manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDefectManifestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered defect manifest $code not found")
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

fun newTieredDefectManifestDesk(store: TieredDefectManifestStore, clock: Clock, flags: Registry): TieredDefectManifestDesk {
    return TieredDefectManifestDesk(
        store = store,
        validator = defaultTieredDefectManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDefectManifestEngine(flags),
    )
}
