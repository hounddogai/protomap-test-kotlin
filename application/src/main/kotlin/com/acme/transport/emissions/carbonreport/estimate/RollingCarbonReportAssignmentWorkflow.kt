package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.Clock

/**
 * Moves rolling carbon report assignments through their life cycle and keeps a journal of what it did.
 */
data class RollingCarbonReportAssignmentWorkflow(
    var store: RollingCarbonReportAssignmentStore,
    var validator: RollingCarbonReportAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling carbon report assignment $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCarbonReportAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling carbon report assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCarbonReportAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRollingCarbonReportAssignmentWorkflow(store: RollingCarbonReportAssignmentStore, clock: Clock): RollingCarbonReportAssignmentWorkflow {
    return RollingCarbonReportAssignmentWorkflow(
        store = store,
        validator = defaultRollingCarbonReportAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
