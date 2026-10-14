package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.clock.Clock

/** Moves manual lease return forecasts through their life cycle and keeps a journal of what it did. */
data class ManualLeaseReturnForecastWorkflow(
    var store: ManualLeaseReturnForecastStore,
    var validator: ManualLeaseReturnForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual lease return forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ManualLeaseReturnForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual lease return forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualLeaseReturnForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newManualLeaseReturnForecastWorkflow(store: ManualLeaseReturnForecastStore, clock: Clock): ManualLeaseReturnForecastWorkflow {
    return ManualLeaseReturnForecastWorkflow(
        store = store,
        validator = defaultManualLeaseReturnForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
