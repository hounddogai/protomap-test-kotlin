package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.lastmile.clock.Clock

/** Moves fallback drop metrics through their life cycle and keeps a journal of what it did. */
data class FallbackDropMetricCoordinator(
    var store: FallbackDropMetricStore,
    var validator: FallbackDropMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback drop metric $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDropMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback drop metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDropMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newFallbackDropMetricCoordinator(store: FallbackDropMetricStore, clock: Clock): FallbackDropMetricCoordinator {
    return FallbackDropMetricCoordinator(
        store = store,
        validator = defaultFallbackDropMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
