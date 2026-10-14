package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary capacity scores through their life cycle and keeps a journal of what it did. */
data class PrimaryCapacityScoreManager(
    var store: PrimaryCapacityScoreStore,
    var validator: PrimaryCapacityScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCapacityScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary capacity score $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCapacityScoreStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary capacity score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCapacityScoreStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary capacity score $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newPrimaryCapacityScoreManager(store: PrimaryCapacityScoreStore, clock: Clock, flags: Registry): PrimaryCapacityScoreManager {
    return PrimaryCapacityScoreManager(
        store = store,
        validator = defaultPrimaryCapacityScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCapacityScoreEngine(flags),
    )
}
