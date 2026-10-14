package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.clock.Clock

/** Moves manual accessorial metrics through their life cycle and keeps a journal of what it did. */
data class ManualAccessorialMetricWorkflow(
    var store: ManualAccessorialMetricStore,
    var validator: ManualAccessorialMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual accessorial metric $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAccessorialMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual accessorial metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAccessorialMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newManualAccessorialMetricWorkflow(store: ManualAccessorialMetricStore, clock: Clock): ManualAccessorialMetricWorkflow {
    return ManualAccessorialMetricWorkflow(
        store = store,
        validator = defaultManualAccessorialMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
