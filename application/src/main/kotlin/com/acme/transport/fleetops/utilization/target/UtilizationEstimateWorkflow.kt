package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization estimates through their life cycle and keeps a journal of what it did. */
data class UtilizationEstimateService(
    var store: UtilizationEstimateStore,
    var validator: UtilizationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("utilization estimate $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization estimate $code not found")
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

fun newUtilizationEstimateService(store: UtilizationEstimateStore, clock: Clock): UtilizationEstimateService {
    return UtilizationEstimateService(
        store = store,
        validator = defaultUtilizationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationEstimateEngine(),
    )
}
