package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.lastmile.clock.Clock

/** Moves regional redelivery entries through their life cycle and keeps a journal of what it did. */
data class RegionalRedeliveryEntryService(
    var store: RegionalRedeliveryEntryStore,
    var validator: RegionalRedeliveryEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRedeliveryEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional redelivery entry $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRedeliveryEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional redelivery entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRedeliveryEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional redelivery entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalRedeliveryEntryService(store: RegionalRedeliveryEntryStore, clock: Clock): RegionalRedeliveryEntryService {
    return RegionalRedeliveryEntryService(
        store = store,
        validator = defaultRegionalRedeliveryEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRedeliveryEntryEngine(),
    )
}
