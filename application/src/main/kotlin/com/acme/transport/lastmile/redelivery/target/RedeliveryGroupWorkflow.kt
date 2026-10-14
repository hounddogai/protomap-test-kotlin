package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery groups through their life cycle and keeps a journal of what it did. */
data class RedeliveryGroupCoordinator(
    var store: RedeliveryGroupStore,
    var validator: RedeliveryGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("redelivery group $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("redelivery group $code not found")
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

fun newRedeliveryGroupCoordinator(store: RedeliveryGroupStore, clock: Clock): RedeliveryGroupCoordinator {
    return RedeliveryGroupCoordinator(
        store = store,
        validator = defaultRedeliveryGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryGroupEngine(),
    )
}
