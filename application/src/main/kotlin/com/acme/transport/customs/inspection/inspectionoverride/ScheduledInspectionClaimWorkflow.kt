package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled inspection claims through their life cycle and keeps a journal of what it did. */
data class ScheduledInspectionClaimService(
    var store: ScheduledInspectionClaimStore,
    var validator: ScheduledInspectionClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledInspectionClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled inspection claim $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledInspectionClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled inspection claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledInspectionClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled inspection claim $code not found")
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

fun newScheduledInspectionClaimService(store: ScheduledInspectionClaimStore, clock: Clock, flags: Registry): ScheduledInspectionClaimService {
    return ScheduledInspectionClaimService(
        store = store,
        validator = defaultScheduledInspectionClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledInspectionClaimEngine(flags),
    )
}
