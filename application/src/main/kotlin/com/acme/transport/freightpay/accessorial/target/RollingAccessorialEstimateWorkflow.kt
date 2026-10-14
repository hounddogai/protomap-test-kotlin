package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling accessorial estimates through their life cycle and keeps a journal of what it did. */
data class RollingAccessorialEstimateWorkflow(
    var store: RollingAccessorialEstimateStore,
    var validator: RollingAccessorialEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingAccessorialEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling accessorial estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAccessorialEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling accessorial estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAccessorialEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling accessorial estimate $code not found")
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

fun newRollingAccessorialEstimateWorkflow(store: RollingAccessorialEstimateStore, clock: Clock, flags: Registry): RollingAccessorialEstimateWorkflow {
    return RollingAccessorialEstimateWorkflow(
        store = store,
        validator = defaultRollingAccessorialEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingAccessorialEstimateEngine(flags),
    )
}
