package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled redelivery entries through their life cycle and keeps a journal of what it did. */
data class ScheduledRedeliveryEntryCoordinator(
    var store: ScheduledRedeliveryEntryStore,
    var validator: ScheduledRedeliveryEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRedeliveryEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled redelivery entry $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRedeliveryEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled redelivery entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRedeliveryEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled redelivery entry $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledRedeliveryEntryCoordinator(store: ScheduledRedeliveryEntryStore, clock: Clock): ScheduledRedeliveryEntryCoordinator {
    return ScheduledRedeliveryEntryCoordinator(
        store = store,
        validator = defaultScheduledRedeliveryEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRedeliveryEntryEngine(),
    )
}
