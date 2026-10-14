package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves strict redelivery entries through their life cycle and keeps a journal of what it did. */
data class StrictRedeliveryEntryService(
    var store: StrictRedeliveryEntryStore,
    var validator: StrictRedeliveryEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRedeliveryEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict redelivery entry $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRedeliveryEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict redelivery entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRedeliveryEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict redelivery entry $code not found")
        val result = engine.evaluate(item)
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

fun newStrictRedeliveryEntryService(store: StrictRedeliveryEntryStore, clock: Clock, flags: Registry): StrictRedeliveryEntryService {
    return StrictRedeliveryEntryService(
        store = store,
        validator = defaultStrictRedeliveryEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRedeliveryEntryEngine(flags),
    )
}
