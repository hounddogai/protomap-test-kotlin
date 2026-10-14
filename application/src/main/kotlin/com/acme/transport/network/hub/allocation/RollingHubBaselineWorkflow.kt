package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.Clock

/** Moves rolling hub baselines through their life cycle and keeps a journal of what it did. */
data class RollingHubBaselineWorkflow(
    var store: RollingHubBaselineStore,
    var validator: RollingHubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingHubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(RollingHubBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingHubBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling hub baseline $code not found")
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

fun newRollingHubBaselineWorkflow(store: RollingHubBaselineStore, clock: Clock): RollingHubBaselineWorkflow {
    return RollingHubBaselineWorkflow(
        store = store,
        validator = defaultRollingHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingHubBaselineEngine(),
    )
}
