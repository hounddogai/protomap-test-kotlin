package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.fleetops.clock.Clock

/** Moves regional lease return plans through their life cycle and keeps a journal of what it did. */
data class RegionalLeaseReturnPlanWorkflow(
    var store: RegionalLeaseReturnPlanStore,
    var validator: RegionalLeaseReturnPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLeaseReturnPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional lease return plan $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLeaseReturnPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lease return plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLeaseReturnPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional lease return plan $code not found")
        val result = engine.evaluate(item)
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

fun newRegionalLeaseReturnPlanWorkflow(store: RegionalLeaseReturnPlanStore, clock: Clock): RegionalLeaseReturnPlanWorkflow {
    return RegionalLeaseReturnPlanWorkflow(
        store = store,
        validator = defaultRegionalLeaseReturnPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLeaseReturnPlanEngine(),
    )
}
