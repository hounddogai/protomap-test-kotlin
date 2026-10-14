package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery segments through their life cycle and keeps a journal of what it did. */
data class RedeliverySegmentManager(
    var store: RedeliverySegmentStore,
    var validator: RedeliverySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliverySegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("redelivery segment $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliverySegmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliverySegmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery segment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRedeliverySegmentManager(store: RedeliverySegmentStore, clock: Clock, flags: Registry): RedeliverySegmentManager {
    return RedeliverySegmentManager(
        store = store,
        validator = defaultRedeliverySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliverySegmentEngine(flags),
    )
}
