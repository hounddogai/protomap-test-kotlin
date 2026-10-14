package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves adaptive dock door metrics through their life cycle and keeps a journal of what it did. */
data class AdaptiveDockDoorMetricService(
    var store: AdaptiveDockDoorMetricStore,
    var validator: AdaptiveDockDoorMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDockDoorMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive dock door metric $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDockDoorMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dock door metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDockDoorMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive dock door metric $code not found")
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

fun newAdaptiveDockDoorMetricService(store: AdaptiveDockDoorMetricStore, clock: Clock, flags: Registry): AdaptiveDockDoorMetricService {
    return AdaptiveDockDoorMetricService(
        store = store,
        validator = defaultAdaptiveDockDoorMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDockDoorMetricEngine(flags),
    )
}
