package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary ev charge segments through their life cycle and keeps a journal of what it did. */
data class PrimaryEvChargeSegmentService(
    var store: PrimaryEvChargeSegmentStore,
    var validator: PrimaryEvChargeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryEvChargeSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary ev charge segment $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryEvChargeSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary ev charge segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryEvChargeSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary ev charge segment $code not found")
        val result = engine.compute(item)
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

fun newPrimaryEvChargeSegmentService(store: PrimaryEvChargeSegmentStore, clock: Clock, flags: Registry): PrimaryEvChargeSegmentService {
    return PrimaryEvChargeSegmentService(
        store = store,
        validator = defaultPrimaryEvChargeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryEvChargeSegmentEngine(flags),
    )
}
