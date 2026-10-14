package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled defect profiles through their life cycle and keeps a journal of what it did. */
data class ScheduledDefectProfileService(
    var store: ScheduledDefectProfileStore,
    var validator: ScheduledDefectProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDefectProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled defect profile $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDefectProfileStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled defect profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDefectProfileStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled defect profile $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledDefectProfileService(store: ScheduledDefectProfileStore, clock: Clock, flags: Registry): ScheduledDefectProfileService {
    return ScheduledDefectProfileService(
        store = store,
        validator = defaultScheduledDefectProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDefectProfileEngine(flags),
    )
}
