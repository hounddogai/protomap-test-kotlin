package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.clock.Clock

/** Moves capped carbon report groups through their life cycle and keeps a journal of what it did. */
data class CappedCarbonReportGroupService(
    var store: CappedCarbonReportGroupStore,
    var validator: CappedCarbonReportGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCarbonReportGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped carbon report group $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCarbonReportGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped carbon report group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCarbonReportGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped carbon report group $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCappedCarbonReportGroupService(store: CappedCarbonReportGroupStore, clock: Clock): CappedCarbonReportGroupService {
    return CappedCarbonReportGroupService(
        store = store,
        validator = defaultCappedCarbonReportGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCarbonReportGroupEngine(),
    )
}
