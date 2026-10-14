package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling rate card entries through their life cycle and keeps a journal of what it did. */
data class RollingRateCardEntryCoordinator(
    var store: RollingRateCardEntryStore,
    var validator: RollingRateCardEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRateCardEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling rate card entry $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRateCardEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling rate card entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRateCardEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling rate card entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRollingRateCardEntryCoordinator(store: RollingRateCardEntryStore, clock: Clock, flags: Registry): RollingRateCardEntryCoordinator {
    return RollingRateCardEntryCoordinator(
        store = store,
        validator = defaultRollingRateCardEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRateCardEntryEngine(flags),
    )
}
