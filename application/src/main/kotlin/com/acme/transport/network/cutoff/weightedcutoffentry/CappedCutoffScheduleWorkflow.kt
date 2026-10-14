package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves capped cutoff schedules through their life cycle and keeps a journal of what it did. */
data class CappedCutoffScheduleManager(
    var store: CappedCutoffScheduleStore,
    var validator: CappedCutoffScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCutoffScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped cutoff schedule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCutoffScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped cutoff schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCutoffScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped cutoff schedule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newCappedCutoffScheduleManager(store: CappedCutoffScheduleStore, clock: Clock, flags: Registry): CappedCutoffScheduleManager {
    return CappedCutoffScheduleManager(
        store = store,
        validator = defaultCappedCutoffScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCutoffScheduleEngine(flags),
    )
}
