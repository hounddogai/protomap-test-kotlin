package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan records through their life cycle and keeps a journal of what it did. */
data class LanePlanRecordManager(
    var store: LanePlanRecordStore,
    var validator: LanePlanRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lane plan record $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan record $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newLanePlanRecordManager(store: LanePlanRecordStore, clock: Clock, flags: Registry): LanePlanRecordManager {
    return LanePlanRecordManager(
        store = store,
        validator = defaultLanePlanRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanRecordEngine(flags),
    )
}
