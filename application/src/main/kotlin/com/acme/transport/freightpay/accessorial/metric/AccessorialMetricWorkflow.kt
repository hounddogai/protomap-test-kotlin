package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial metrics through their life cycle and keeps a journal of what it did. */
data class AccessorialMetricCoordinator(
    var store: AccessorialMetricStore,
    var validator: AccessorialMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial metric $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAccessorialMetricCoordinator(store: AccessorialMetricStore, clock: Clock): AccessorialMetricCoordinator {
    return AccessorialMetricCoordinator(
        store = store,
        validator = defaultAccessorialMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
