package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary dispute metrics through their life cycle and keeps a journal of what it did. */
data class PrimaryDisputeMetricService(
    var store: PrimaryDisputeMetricStore,
    var validator: PrimaryDisputeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDisputeMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary dispute metric $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDisputeMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary dispute metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDisputeMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary dispute metric $code not found")
        val result = engine.run(item)
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

fun newPrimaryDisputeMetricService(store: PrimaryDisputeMetricStore, clock: Clock, flags: Registry): PrimaryDisputeMetricService {
    return PrimaryDisputeMetricService(
        store = store,
        validator = defaultPrimaryDisputeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDisputeMetricEngine(flags),
    )
}
