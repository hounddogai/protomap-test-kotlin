package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves manual backhaul overrides through their life cycle and keeps a journal of what it did. */
data class ManualBackhaulOverrideCoordinator(
    var store: ManualBackhaulOverrideStore,
    var validator: ManualBackhaulOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualBackhaulOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual backhaul override $code not found")
        validator.requireValid(item)
        item.moveTo(ManualBackhaulOverrideStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual backhaul override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualBackhaulOverrideStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual backhaul override $code not found")
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

fun newManualBackhaulOverrideCoordinator(store: ManualBackhaulOverrideStore, clock: Clock, flags: Registry): ManualBackhaulOverrideCoordinator {
    return ManualBackhaulOverrideCoordinator(
        store = store,
        validator = defaultManualBackhaulOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualBackhaulOverrideEngine(flags),
    )
}
