package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal tire records through their life cycle and keeps a journal of what it did. */
data class SeasonalTireRecordWorkflow(
    var store: SeasonalTireRecordStore,
    var validator: SeasonalTireRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTireRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal tire record $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTireRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tire record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTireRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal tire record $code not found")
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

fun newSeasonalTireRecordWorkflow(store: SeasonalTireRecordStore, clock: Clock, flags: Registry): SeasonalTireRecordWorkflow {
    return SeasonalTireRecordWorkflow(
        store = store,
        validator = defaultSeasonalTireRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTireRecordEngine(flags),
    )
}
