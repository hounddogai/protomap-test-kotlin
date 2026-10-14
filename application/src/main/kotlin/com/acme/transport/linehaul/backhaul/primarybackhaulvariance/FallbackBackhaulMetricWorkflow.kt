package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback backhaul metrics through their life cycle and keeps a journal of what it did. */
data class FallbackBackhaulMetricCoordinator(
    var store: FallbackBackhaulMetricStore,
    var validator: FallbackBackhaulMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback backhaul metric $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackBackhaulMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback backhaul metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackBackhaulMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newFallbackBackhaulMetricCoordinator(store: FallbackBackhaulMetricStore, clock: Clock): FallbackBackhaulMetricCoordinator {
    return FallbackBackhaulMetricCoordinator(
        store = store,
        validator = defaultFallbackBackhaulMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
