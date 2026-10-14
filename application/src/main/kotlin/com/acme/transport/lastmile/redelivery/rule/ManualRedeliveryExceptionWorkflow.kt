package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves manual redelivery exceptions through their life cycle and keeps a journal of what it did. */
data class ManualRedeliveryExceptionManager(
    var store: ManualRedeliveryExceptionStore,
    var validator: ManualRedeliveryExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRedeliveryExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual redelivery exception $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRedeliveryExceptionStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual redelivery exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRedeliveryExceptionStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual redelivery exception $code not found")
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

fun newManualRedeliveryExceptionManager(store: ManualRedeliveryExceptionStore, clock: Clock, flags: Registry): ManualRedeliveryExceptionManager {
    return ManualRedeliveryExceptionManager(
        store = store,
        validator = defaultManualRedeliveryExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRedeliveryExceptionEngine(flags),
    )
}
