package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect claims through their life cycle and keeps a journal of what it did. */
data class DefectClaimCoordinator(
    var store: DefectClaimStore,
    var validator: DefectClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefectClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("defect claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("defect claim $code not found")
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

fun newDefectClaimCoordinator(store: DefectClaimStore, clock: Clock, flags: Registry): DefectClaimCoordinator {
    return DefectClaimCoordinator(
        store = store,
        validator = defaultDefectClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectClaimEngine(flags),
    )
}
