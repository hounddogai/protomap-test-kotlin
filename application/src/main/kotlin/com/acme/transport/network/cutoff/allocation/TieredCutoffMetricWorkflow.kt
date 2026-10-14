package com.acme.transport.network.cutoff.allocation

import com.acme.transport.network.clock.Clock

/** Moves tiered cutoff metrics through their life cycle and keeps a journal of what it did. */
data class TieredCutoffMetricWorkflow(
    var store: TieredCutoffMetricStore,
    var validator: TieredCutoffMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered cutoff metric $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCutoffMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cutoff metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCutoffMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTieredCutoffMetricWorkflow(store: TieredCutoffMetricStore, clock: Clock): TieredCutoffMetricWorkflow {
    return TieredCutoffMetricWorkflow(
        store = store,
        validator = defaultTieredCutoffMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
