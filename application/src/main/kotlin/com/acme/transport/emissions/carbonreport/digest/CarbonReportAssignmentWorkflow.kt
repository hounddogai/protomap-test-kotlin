package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report assignments through their life cycle and keeps a journal of what it did. */
data class CarbonReportAssignmentService(
    var store: CarbonReportAssignmentStore,
    var validator: CarbonReportAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCarbonReportAssignmentService(store: CarbonReportAssignmentStore, clock: Clock): CarbonReportAssignmentService {
    return CarbonReportAssignmentService(
        store = store,
        validator = defaultCarbonReportAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
