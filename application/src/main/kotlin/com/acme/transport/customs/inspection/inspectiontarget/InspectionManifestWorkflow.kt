package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.customs.clock.Clock

/** Moves inspection manifests through their life cycle and keeps a journal of what it did. */
data class InspectionManifestService(
    var store: InspectionManifestStore,
    var validator: InspectionManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection manifest $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newInspectionManifestService(store: InspectionManifestStore, clock: Clock): InspectionManifestService {
    return InspectionManifestService(
        store = store,
        validator = defaultInspectionManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionManifestEngine(),
    )
}
