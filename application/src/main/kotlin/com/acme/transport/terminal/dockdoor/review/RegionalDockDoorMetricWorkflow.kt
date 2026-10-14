package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves regional dock door metrics through their life cycle and keeps a journal of what it did. */
data class RegionalDockDoorMetricManager(
    var store: RegionalDockDoorMetricStore,
    var validator: RegionalDockDoorMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDockDoorMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional dock door metric $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDockDoorMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional dock door metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDockDoorMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional dock door metric $code not found")
        val result = engine.run(item)
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

fun newRegionalDockDoorMetricManager(store: RegionalDockDoorMetricStore, clock: Clock, flags: Registry): RegionalDockDoorMetricManager {
    return RegionalDockDoorMetricManager(
        store = store,
        validator = defaultRegionalDockDoorMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDockDoorMetricEngine(flags),
    )
}
