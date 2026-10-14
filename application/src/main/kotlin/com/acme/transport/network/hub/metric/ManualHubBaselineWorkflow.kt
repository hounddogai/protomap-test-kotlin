package com.acme.transport.network.hub.metric

import com.acme.transport.network.clock.Clock

/** Moves manual hub baselines through their life cycle and keeps a journal of what it did. */
data class ManualHubBaselineDesk(
    var store: ManualHubBaselineStore,
    var validator: ManualHubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualHubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ManualHubBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualHubBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual hub baseline $code not found")
        val result = engine.run(item)
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

fun newManualHubBaselineDesk(store: ManualHubBaselineStore, clock: Clock): ManualHubBaselineDesk {
    return ManualHubBaselineDesk(
        store = store,
        validator = defaultManualHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualHubBaselineEngine(),
    )
}
