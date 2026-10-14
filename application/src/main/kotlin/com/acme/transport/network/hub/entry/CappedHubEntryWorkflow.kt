package com.acme.transport.network.hub.entry

import com.acme.transport.network.clock.Clock

/** Moves capped hub entries through their life cycle and keeps a journal of what it did. */
data class CappedHubEntryWorkflow(
    var store: CappedHubEntryStore,
    var validator: CappedHubEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedHubEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped hub entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedHubEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped hub entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedHubEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped hub entry $code not found")
        val result = engine.evaluate(item)
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

fun newCappedHubEntryWorkflow(store: CappedHubEntryStore, clock: Clock): CappedHubEntryWorkflow {
    return CappedHubEntryWorkflow(
        store = store,
        validator = defaultCappedHubEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedHubEntryEngine(),
    )
}
