package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves regional parcel windows through their life cycle and keeps a journal of what it did. */
data class RegionalParcelWindowService(
    var store: RegionalParcelWindowStore,
    var validator: RegionalParcelWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalParcelWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional parcel window $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalParcelWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional parcel window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalParcelWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional parcel window $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRegionalParcelWindowService(store: RegionalParcelWindowStore, clock: Clock, flags: Registry): RegionalParcelWindowService {
    return RegionalParcelWindowService(
        store = store,
        validator = defaultRegionalParcelWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalParcelWindowEngine(flags),
    )
}
