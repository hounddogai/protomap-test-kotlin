package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.terminal.clock.Clock

/** Moves dwell rules through their life cycle and keeps a journal of what it did. */
data class DwellRuleCoordinator(
    var store: DwellRuleStore,
    var validator: DwellRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dwell rule $code not found")
        validator.requireValid(item)
        item.moveTo(DwellRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("dwell rule $code not found")
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

fun newDwellRuleCoordinator(store: DwellRuleStore, clock: Clock): DwellRuleCoordinator {
    return DwellRuleCoordinator(
        store = store,
        validator = defaultDwellRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellRuleEngine(),
    )
}
