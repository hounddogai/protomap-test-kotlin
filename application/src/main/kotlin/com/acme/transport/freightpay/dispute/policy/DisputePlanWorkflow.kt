package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute plans through their life cycle and keeps a journal of what it did. */
data class DisputePlanService(
    var store: DisputePlanStore,
    var validator: DisputePlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputePlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dispute plan $code not found")
        validator.requireValid(item)
        item.moveTo(DisputePlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputePlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dispute plan $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newDisputePlanService(store: DisputePlanStore, clock: Clock, flags: Registry): DisputePlanService {
    return DisputePlanService(
        store = store,
        validator = defaultDisputePlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputePlanEngine(flags),
    )
}
