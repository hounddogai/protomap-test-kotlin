package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery entries through their life cycle and keeps a journal of what it did. */
data class RedeliveryEntryService(
    var store: RedeliveryEntryStore,
    var validator: RedeliveryEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("redelivery entry $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("redelivery entry $code not found")
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

fun newRedeliveryEntryService(store: RedeliveryEntryStore, clock: Clock): RedeliveryEntryService {
    return RedeliveryEntryService(
        store = store,
        validator = defaultRedeliveryEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryEntryEngine(),
    )
}
