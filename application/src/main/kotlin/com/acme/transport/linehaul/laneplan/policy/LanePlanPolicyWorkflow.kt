package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan policies through their life cycle and keeps a journal of what it did. */
data class LanePlanPolicyWorkflow(
    var store: LanePlanPolicyStore,
    var validator: LanePlanPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lane plan policy $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newLanePlanPolicyWorkflow(store: LanePlanPolicyStore, clock: Clock): LanePlanPolicyWorkflow {
    return LanePlanPolicyWorkflow(
        store = store,
        validator = defaultLanePlanPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
