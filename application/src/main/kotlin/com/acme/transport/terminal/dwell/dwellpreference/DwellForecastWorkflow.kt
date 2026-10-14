package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dwell forecasts through their life cycle and keeps a journal of what it did. */
data class DwellForecastDesk(
    var store: DwellForecastStore,
    var validator: DwellForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dwell forecast $code not found")
        validator.requireValid(item)
        item.moveTo(DwellForecastStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellForecastStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dwell forecast $code not found")
        val result = engine.run(item)
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

fun newDwellForecastDesk(store: DwellForecastStore, clock: Clock, flags: Registry): DwellForecastDesk {
    return DwellForecastDesk(
        store = store,
        validator = defaultDwellForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellForecastEngine(flags),
    )
}
