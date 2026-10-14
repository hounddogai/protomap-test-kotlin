package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped duty period policies through their life cycle and keeps a journal of what it did. */
data class CappedDutyPeriodPolicyDesk(
    var store: CappedDutyPeriodPolicyStore,
    var validator: CappedDutyPeriodPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDutyPeriodPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped duty period policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDutyPeriodPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped duty period policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDutyPeriodPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped duty period policy $code not found")
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

fun newCappedDutyPeriodPolicyDesk(store: CappedDutyPeriodPolicyStore, clock: Clock, flags: Registry): CappedDutyPeriodPolicyDesk {
    return CappedDutyPeriodPolicyDesk(
        store = store,
        validator = defaultCappedDutyPeriodPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDutyPeriodPolicyEngine(flags),
    )
}
