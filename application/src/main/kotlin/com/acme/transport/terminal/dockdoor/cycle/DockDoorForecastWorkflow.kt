package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door forecasts through their life cycle and keeps a journal of what it did. */
data class DockDoorForecastWorkflow(
    var store: DockDoorForecastStore,
    var validator: DockDoorForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dock door forecast $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dock door forecast $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDockDoorForecastWorkflow(store: DockDoorForecastStore, clock: Clock, flags: Registry): DockDoorForecastWorkflow {
    return DockDoorForecastWorkflow(
        store = store,
        validator = defaultDockDoorForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorForecastEngine(flags),
    )
}
