package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves pending near miss thresholds through their life cycle and keeps a journal of what it did. */
data class PendingNearMissThresholdService(
    var store: PendingNearMissThresholdStore,
    var validator: PendingNearMissThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingNearMissThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending near miss threshold $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNearMissThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending near miss threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNearMissThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending near miss threshold $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newPendingNearMissThresholdService(store: PendingNearMissThresholdStore, clock: Clock, flags: Registry): PendingNearMissThresholdService {
    return PendingNearMissThresholdService(
        store = store,
        validator = defaultPendingNearMissThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingNearMissThresholdEngine(flags),
    )
}
