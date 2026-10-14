package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending duty period policies through their life cycle and keeps a journal of what it did. */
data class PendingDutyPeriodPolicyDesk(
    var store: PendingDutyPeriodPolicyStore,
    var validator: PendingDutyPeriodPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingDutyPeriodPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending duty period policy $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDutyPeriodPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending duty period policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDutyPeriodPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending duty period policy $code not found")
        val result = engine.run(item)
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

fun newPendingDutyPeriodPolicyDesk(store: PendingDutyPeriodPolicyStore, clock: Clock, flags: Registry): PendingDutyPeriodPolicyDesk {
    return PendingDutyPeriodPolicyDesk(
        store = store,
        validator = defaultPendingDutyPeriodPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingDutyPeriodPolicyEngine(flags),
    )
}
