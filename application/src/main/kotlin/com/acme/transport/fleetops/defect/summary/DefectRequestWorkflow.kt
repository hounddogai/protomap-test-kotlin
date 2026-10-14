package com.acme.transport.fleetops.defect.summary

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect requests through their life cycle and keeps a journal of what it did. */
data class DefectRequestCoordinator(
    var store: DefectRequestStore,
    var validator: DefectRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("defect request $code not found")
        validator.requireValid(item)
        item.moveTo(DefectRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("defect request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("defect request $code not found")
        val result = engine.run(item)
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

fun newDefectRequestCoordinator(store: DefectRequestStore, clock: Clock, flags: Registry): DefectRequestCoordinator {
    return DefectRequestCoordinator(
        store = store,
        validator = defaultDefectRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectRequestEngine(flags),
    )
}
