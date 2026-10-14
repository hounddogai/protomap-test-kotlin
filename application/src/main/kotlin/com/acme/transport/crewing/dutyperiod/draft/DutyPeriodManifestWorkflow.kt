package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves duty period manifests through their life cycle and keeps a journal of what it did. */
data class DutyPeriodManifestCoordinator(
    var store: DutyPeriodManifestStore,
    var validator: DutyPeriodManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("duty period manifest $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("duty period manifest $code not found")
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

fun newDutyPeriodManifestCoordinator(store: DutyPeriodManifestStore, clock: Clock, flags: Registry): DutyPeriodManifestCoordinator {
    return DutyPeriodManifestCoordinator(
        store = store,
        validator = defaultDutyPeriodManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodManifestEngine(flags),
    )
}
