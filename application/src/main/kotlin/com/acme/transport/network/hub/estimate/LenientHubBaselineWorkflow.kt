package com.acme.transport.network.hub.estimate

import com.acme.transport.network.clock.Clock

/** Moves lenient hub baselines through their life cycle and keeps a journal of what it did. */
data class LenientHubBaselineWorkflow(
    var store: LenientHubBaselineStore,
    var validator: LenientHubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientHubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LenientHubBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientHubBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient hub baseline $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newLenientHubBaselineWorkflow(store: LenientHubBaselineStore, clock: Clock): LenientHubBaselineWorkflow {
    return LenientHubBaselineWorkflow(
        store = store,
        validator = defaultLenientHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientHubBaselineEngine(),
    )
}
