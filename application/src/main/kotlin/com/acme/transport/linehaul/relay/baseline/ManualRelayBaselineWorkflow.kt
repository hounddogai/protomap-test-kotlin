package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves manual relay baselines through their life cycle and keeps a journal of what it did. */
data class ManualRelayBaselineCoordinator(
    var store: ManualRelayBaselineStore,
    var validator: ManualRelayBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRelayBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual relay baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRelayBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual relay baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRelayBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual relay baseline $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newManualRelayBaselineCoordinator(store: ManualRelayBaselineStore, clock: Clock, flags: Registry): ManualRelayBaselineCoordinator {
    return ManualRelayBaselineCoordinator(
        store = store,
        validator = defaultManualRelayBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRelayBaselineEngine(flags),
    )
}
