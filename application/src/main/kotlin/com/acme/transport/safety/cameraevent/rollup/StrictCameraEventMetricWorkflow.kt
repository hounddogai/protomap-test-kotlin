package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.clock.Clock

/** Moves strict camera event metrics through their life cycle and keeps a journal of what it did. */
data class StrictCameraEventMetricDesk(
    var store: StrictCameraEventMetricStore,
    var validator: StrictCameraEventMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictCameraEventMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict camera event metric $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCameraEventMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict camera event metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCameraEventMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict camera event metric $code not found")
        val result = engine.compute(item)
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

fun newStrictCameraEventMetricDesk(store: StrictCameraEventMetricStore, clock: Clock): StrictCameraEventMetricDesk {
    return StrictCameraEventMetricDesk(
        store = store,
        validator = defaultStrictCameraEventMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictCameraEventMetricEngine(),
    )
}
