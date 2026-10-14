package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute digests through their life cycle and keeps a journal of what it did. */
data class DisputeDigestManager(
    var store: DisputeDigestStore,
    var validator: DisputeDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dispute digest $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dispute digest $code not found")
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

fun newDisputeDigestManager(store: DisputeDigestStore, clock: Clock, flags: Registry): DisputeDigestManager {
    return DisputeDigestManager(
        store = store,
        validator = defaultDisputeDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeDigestEngine(flags),
    )
}
