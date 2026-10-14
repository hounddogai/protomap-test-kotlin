package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.network.clock.Clock

/** Moves adaptive node metrics through their life cycle and keeps a journal of what it did. */
data class AdaptiveNodeMetricWorkflow(
    var store: AdaptiveNodeMetricStore,
    var validator: AdaptiveNodeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive node metric $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveNodeMetricStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive node metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveNodeMetricStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newAdaptiveNodeMetricWorkflow(store: AdaptiveNodeMetricStore, clock: Clock): AdaptiveNodeMetricWorkflow {
    return AdaptiveNodeMetricWorkflow(
        store = store,
        validator = defaultAdaptiveNodeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
