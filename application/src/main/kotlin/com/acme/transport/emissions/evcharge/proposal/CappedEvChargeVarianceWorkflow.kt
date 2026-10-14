package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.clock.Clock

/** Moves capped ev charge variances through their life cycle and keeps a journal of what it did. */
data class CappedEvChargeVarianceCoordinator(
    var store: CappedEvChargeVarianceStore,
    var validator: CappedEvChargeVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped ev charge variance $code not found")
        validator.requireValid(item)
        item.moveTo(CappedEvChargeVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped ev charge variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedEvChargeVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newCappedEvChargeVarianceCoordinator(store: CappedEvChargeVarianceStore, clock: Clock): CappedEvChargeVarianceCoordinator {
    return CappedEvChargeVarianceCoordinator(
        store = store,
        validator = defaultCappedEvChargeVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
