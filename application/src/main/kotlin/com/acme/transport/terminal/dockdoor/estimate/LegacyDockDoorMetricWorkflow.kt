package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.terminal.clock.Clock

/** Moves legacy dock door metrics through their life cycle and keeps a journal of what it did. */
data class LegacyDockDoorMetricWorkflow(
    var store: LegacyDockDoorMetricStore,
    var validator: LegacyDockDoorMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDockDoorMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy dock door metric $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDockDoorMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy dock door metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDockDoorMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy dock door metric $code not found")
        val result = engine.price(item)
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

fun newLegacyDockDoorMetricWorkflow(store: LegacyDockDoorMetricStore, clock: Clock): LegacyDockDoorMetricWorkflow {
    return LegacyDockDoorMetricWorkflow(
        store = store,
        validator = defaultLegacyDockDoorMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDockDoorMetricEngine(),
    )
}
