package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy defect agreements through their life cycle and keeps a journal of what it did. */
data class LegacyDefectAgreementCoordinator(
    var store: LegacyDefectAgreementStore,
    var validator: LegacyDefectAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDefectAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy defect agreement $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDefectAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy defect agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDefectAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy defect agreement $code not found")
        val result = engine.price(item)
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

fun newLegacyDefectAgreementCoordinator(store: LegacyDefectAgreementStore, clock: Clock, flags: Registry): LegacyDefectAgreementCoordinator {
    return LegacyDefectAgreementCoordinator(
        store = store,
        validator = defaultLegacyDefectAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDefectAgreementEngine(flags),
    )
}
