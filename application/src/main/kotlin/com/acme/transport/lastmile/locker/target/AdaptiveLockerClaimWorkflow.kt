package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves adaptive locker claims through their life cycle and keeps a journal of what it did. */
data class AdaptiveLockerClaimWorkflow(
    var store: AdaptiveLockerClaimStore,
    var validator: AdaptiveLockerClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveLockerClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive locker claim $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLockerClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive locker claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLockerClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive locker claim $code not found")
        val result = engine.compute(item)
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

fun newAdaptiveLockerClaimWorkflow(store: AdaptiveLockerClaimStore, clock: Clock, flags: Registry): AdaptiveLockerClaimWorkflow {
    return AdaptiveLockerClaimWorkflow(
        store = store,
        validator = defaultAdaptiveLockerClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveLockerClaimEngine(flags),
    )
}
