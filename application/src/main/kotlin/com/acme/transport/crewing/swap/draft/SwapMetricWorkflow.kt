package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.clock.Clock

/** Moves swap metrics through their life cycle and keeps a journal of what it did. */
data class SwapMetricCoordinator(
    var store: SwapMetricStore,
    var validator: SwapMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("swap metric $code not found")
        validator.requireValid(item)
        item.moveTo(SwapMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("swap metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newSwapMetricCoordinator(store: SwapMetricStore, clock: Clock): SwapMetricCoordinator {
    return SwapMetricCoordinator(
        store = store,
        validator = defaultSwapMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
