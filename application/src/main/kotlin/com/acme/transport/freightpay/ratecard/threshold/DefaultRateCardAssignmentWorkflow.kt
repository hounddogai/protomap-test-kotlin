package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.clock.Clock

/** Moves default rate card assignments through their life cycle and keeps a journal of what it did. */
data class DefaultRateCardAssignmentCoordinator(
    var store: DefaultRateCardAssignmentStore,
    var validator: DefaultRateCardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default rate card assignment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRateCardAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default rate card assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRateCardAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultRateCardAssignmentCoordinator(store: DefaultRateCardAssignmentStore, clock: Clock): DefaultRateCardAssignmentCoordinator {
    return DefaultRateCardAssignmentCoordinator(
        store = store,
        validator = defaultDefaultRateCardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
