package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.emissions.clock.Clock

/**
 * Moves pending emission factor segments through their life cycle and keeps a journal of what it did.
 */
data class PendingEmissionFactorSegmentWorkflow(
    var store: PendingEmissionFactorSegmentStore,
    var validator: PendingEmissionFactorSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending emission factor segment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingEmissionFactorSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending emission factor segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingEmissionFactorSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newPendingEmissionFactorSegmentWorkflow(store: PendingEmissionFactorSegmentStore, clock: Clock): PendingEmissionFactorSegmentWorkflow {
    return PendingEmissionFactorSegmentWorkflow(
        store = store,
        validator = defaultPendingEmissionFactorSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
