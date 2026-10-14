package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves manual dwell overrides through their life cycle and keeps a journal of what it did. */
data class ManualDwellOverrideService(
    var store: ManualDwellOverrideStore,
    var validator: ManualDwellOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDwellOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual dwell override $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDwellOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual dwell override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDwellOverrideStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual dwell override $code not found")
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

fun newManualDwellOverrideService(store: ManualDwellOverrideStore, clock: Clock, flags: Registry): ManualDwellOverrideService {
    return ManualDwellOverrideService(
        store = store,
        validator = defaultManualDwellOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDwellOverrideEngine(flags),
    )
}
