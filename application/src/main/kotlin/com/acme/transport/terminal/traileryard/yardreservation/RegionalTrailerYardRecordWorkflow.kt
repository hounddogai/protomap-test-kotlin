package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves regional trailer yard records through their life cycle and keeps a journal of what it did. */
data class RegionalTrailerYardRecordWorkflow(
    var store: RegionalTrailerYardRecordStore,
    var validator: RegionalTrailerYardRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalTrailerYardRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional trailer yard record $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTrailerYardRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional trailer yard record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTrailerYardRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional trailer yard record $code not found")
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

fun newRegionalTrailerYardRecordWorkflow(store: RegionalTrailerYardRecordStore, clock: Clock, flags: Registry): RegionalTrailerYardRecordWorkflow {
    return RegionalTrailerYardRecordWorkflow(
        store = store,
        validator = defaultRegionalTrailerYardRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalTrailerYardRecordEngine(flags),
    )
}
