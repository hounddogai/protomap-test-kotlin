package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled lane plan quotas through their life cycle and keeps a journal of what it did. */
data class ScheduledLanePlanQuotaWorkflow(
    var store: ScheduledLanePlanQuotaStore,
    var validator: ScheduledLanePlanQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled lane plan quota $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLanePlanQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lane plan quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLanePlanQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newScheduledLanePlanQuotaWorkflow(store: ScheduledLanePlanQuotaStore, clock: Clock): ScheduledLanePlanQuotaWorkflow {
    return ScheduledLanePlanQuotaWorkflow(
        store = store,
        validator = defaultScheduledLanePlanQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
