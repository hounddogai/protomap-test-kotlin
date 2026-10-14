package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan quotas through their life cycle and keeps a journal of what it did. */
data class LanePlanQuotaWorkflow(
    var store: LanePlanQuotaStore,
    var validator: LanePlanQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan quota $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lane plan quota $code not found")
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

fun newLanePlanQuotaWorkflow(store: LanePlanQuotaStore, clock: Clock, flags: Registry): LanePlanQuotaWorkflow {
    return LanePlanQuotaWorkflow(
        store = store,
        validator = defaultLanePlanQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanQuotaEngine(flags),
    )
}
