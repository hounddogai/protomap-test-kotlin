package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.terminal.clock.Clock

/** Moves rolling dwell plans through their life cycle and keeps a journal of what it did. */
data class RollingDwellPlanService(
    var store: RollingDwellPlanStore,
    var validator: RollingDwellPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDwellPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling dwell plan $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDwellPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dwell plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDwellPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling dwell plan $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRollingDwellPlanService(store: RollingDwellPlanStore, clock: Clock): RollingDwellPlanService {
    return RollingDwellPlanService(
        store = store,
        validator = defaultRollingDwellPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDwellPlanEngine(),
    )
}
