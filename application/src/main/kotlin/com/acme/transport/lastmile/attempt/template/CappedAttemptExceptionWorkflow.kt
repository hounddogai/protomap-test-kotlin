package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves capped attempt exceptions through their life cycle and keeps a journal of what it did. */
data class CappedAttemptExceptionWorkflow(
    var store: CappedAttemptExceptionStore,
    var validator: CappedAttemptExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedAttemptExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped attempt exception $code not found")
        validator.requireValid(item)
        item.moveTo(CappedAttemptExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped attempt exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedAttemptExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped attempt exception $code not found")
        val result = engine.compute(item)
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

fun newCappedAttemptExceptionWorkflow(store: CappedAttemptExceptionStore, clock: Clock, flags: Registry): CappedAttemptExceptionWorkflow {
    return CappedAttemptExceptionWorkflow(
        store = store,
        validator = defaultCappedAttemptExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedAttemptExceptionEngine(flags),
    )
}
