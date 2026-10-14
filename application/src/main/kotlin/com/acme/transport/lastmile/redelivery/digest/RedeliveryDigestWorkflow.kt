package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery digests through their life cycle and keeps a journal of what it did. */
data class RedeliveryDigestDesk(
    var store: RedeliveryDigestStore,
    var validator: RedeliveryDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("redelivery digest $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("redelivery digest $code not found")
        val result = engine.evaluate(item)
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

fun newRedeliveryDigestDesk(store: RedeliveryDigestStore, clock: Clock): RedeliveryDigestDesk {
    return RedeliveryDigestDesk(
        store = store,
        validator = defaultRedeliveryDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryDigestEngine(),
    )
}
