package com.acme.transport.network.servicearea.exception

import com.acme.transport.network.clock.Clock

/**
 * Moves regional service area thresholds through their life cycle and keeps a journal of what it did.
 */
data class RegionalServiceAreaThresholdWorkflow(
    var store: RegionalServiceAreaThresholdStore,
    var validator: RegionalServiceAreaThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalServiceAreaThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional service area threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalServiceAreaThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional service area threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalServiceAreaThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional service area threshold $code not found")
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

fun newRegionalServiceAreaThresholdWorkflow(store: RegionalServiceAreaThresholdStore, clock: Clock): RegionalServiceAreaThresholdWorkflow {
    return RegionalServiceAreaThresholdWorkflow(
        store = store,
        validator = defaultRegionalServiceAreaThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalServiceAreaThresholdEngine(),
    )
}
