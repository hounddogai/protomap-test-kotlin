package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan estimates through their life cycle and keeps a journal of what it did. */
data class LanePlanEstimateManager(
    var store: LanePlanEstimateStore,
    var validator: LanePlanEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanEstimateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanEstimateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan estimate $code not found")
        val result = engine.price(item)
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

fun newLanePlanEstimateManager(store: LanePlanEstimateStore, clock: Clock, flags: Registry): LanePlanEstimateManager {
    return LanePlanEstimateManager(
        store = store,
        validator = defaultLanePlanEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanEstimateEngine(flags),
    )
}
