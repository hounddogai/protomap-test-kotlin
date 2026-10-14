package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.lastmile.clock.Clock

/** Moves capped redelivery windows through their life cycle and keeps a journal of what it did. */
data class CappedRedeliveryWindowManager(
    var store: CappedRedeliveryWindowStore,
    var validator: CappedRedeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped redelivery window $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRedeliveryWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped redelivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRedeliveryWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCappedRedeliveryWindowManager(store: CappedRedeliveryWindowStore, clock: Clock): CappedRedeliveryWindowManager {
    return CappedRedeliveryWindowManager(
        store = store,
        validator = defaultCappedRedeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
