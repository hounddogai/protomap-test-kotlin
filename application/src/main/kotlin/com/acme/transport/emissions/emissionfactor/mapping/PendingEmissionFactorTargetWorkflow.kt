package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.clock.Clock

/**
 * Moves pending emission factor targets through their life cycle and keeps a journal of what it did.
 */
data class PendingEmissionFactorTargetManager(
    var store: PendingEmissionFactorTargetStore,
    var validator: PendingEmissionFactorTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending emission factor target $code not found")
        validator.requireValid(item)
        item.moveTo(PendingEmissionFactorTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending emission factor target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingEmissionFactorTargetStatus.CANCELLED)
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

fun newPendingEmissionFactorTargetManager(store: PendingEmissionFactorTargetStore, clock: Clock): PendingEmissionFactorTargetManager {
    return PendingEmissionFactorTargetManager(
        store = store,
        validator = defaultPendingEmissionFactorTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
