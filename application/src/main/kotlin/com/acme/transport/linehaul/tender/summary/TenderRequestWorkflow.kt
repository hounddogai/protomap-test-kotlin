package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.clock.Clock

/** Moves tender requests through their life cycle and keeps a journal of what it did. */
data class TenderRequestWorkflow(
    var store: TenderRequestStore,
    var validator: TenderRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender request $code not found")
        validator.requireValid(item)
        item.moveTo(TenderRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tender request $code not found")
        val result = engine.compute(item)
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

fun newTenderRequestWorkflow(store: TenderRequestStore, clock: Clock): TenderRequestWorkflow {
    return TenderRequestWorkflow(
        store = store,
        validator = defaultTenderRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderRequestEngine(),
    )
}
