package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.emissions.clock.Clock

/** Moves idling overrides through their life cycle and keeps a journal of what it did. */
data class IdlingOverrideWorkflow(
    var store: IdlingOverrideStore,
    var validator: IdlingOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling override $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("idling override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling override $code not found")
        val result = engine.price(item)
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

fun newIdlingOverrideWorkflow(store: IdlingOverrideStore, clock: Clock): IdlingOverrideWorkflow {
    return IdlingOverrideWorkflow(
        store = store,
        validator = defaultIdlingOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingOverrideEngine(),
    )
}
