package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect scores through their life cycle and keeps a journal of what it did. */
data class DefectScoreManager(
    var store: DefectScoreStore,
    var validator: DefectScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("defect score $code not found")
        validator.requireValid(item)
        item.moveTo(DefectScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("defect score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("defect score $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDefectScoreManager(store: DefectScoreStore, clock: Clock, flags: Registry): DefectScoreManager {
    return DefectScoreManager(
        store = store,
        validator = defaultDefectScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectScoreEngine(flags),
    )
}
