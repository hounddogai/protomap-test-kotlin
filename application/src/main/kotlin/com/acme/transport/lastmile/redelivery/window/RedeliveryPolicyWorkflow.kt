package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery policies through their life cycle and keeps a journal of what it did. */
data class RedeliveryPolicyDesk(
    var store: RedeliveryPolicyStore,
    var validator: RedeliveryPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("redelivery policy $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("redelivery policy $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRedeliveryPolicyDesk(store: RedeliveryPolicyStore, clock: Clock): RedeliveryPolicyDesk {
    return RedeliveryPolicyDesk(
        store = store,
        validator = defaultRedeliveryPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryPolicyEngine(),
    )
}
