package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves strict zone records through their life cycle and keeps a journal of what it did. */
data class StrictZoneRecordManager(
    var store: StrictZoneRecordStore,
    var validator: StrictZoneRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictZoneRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict zone record $code not found")
        validator.requireValid(item)
        item.moveTo(StrictZoneRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict zone record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictZoneRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict zone record $code not found")
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

fun newStrictZoneRecordManager(store: StrictZoneRecordStore, clock: Clock, flags: Registry): StrictZoneRecordManager {
    return StrictZoneRecordManager(
        store = store,
        validator = defaultStrictZoneRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictZoneRecordEngine(flags),
    )
}
