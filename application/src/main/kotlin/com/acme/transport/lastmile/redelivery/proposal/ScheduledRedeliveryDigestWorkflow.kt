package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled redelivery digests through their life cycle and keeps a journal of what it did. */
data class ScheduledRedeliveryDigestDesk(
    var store: ScheduledRedeliveryDigestStore,
    var validator: ScheduledRedeliveryDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRedeliveryDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled redelivery digest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRedeliveryDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled redelivery digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRedeliveryDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled redelivery digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newScheduledRedeliveryDigestDesk(store: ScheduledRedeliveryDigestStore, clock: Clock, flags: Registry): ScheduledRedeliveryDigestDesk {
    return ScheduledRedeliveryDigestDesk(
        store = store,
        validator = defaultScheduledRedeliveryDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRedeliveryDigestEngine(flags),
    )
}
