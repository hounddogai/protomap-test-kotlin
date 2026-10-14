package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.clock.Clock

/**
 * Moves default emission factor snapshots through their life cycle and keeps a journal of what it did.
 */
data class DefaultEmissionFactorSnapshotDesk(
    var store: DefaultEmissionFactorSnapshotStore,
    var validator: DefaultEmissionFactorSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default emission factor snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEmissionFactorSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default emission factor snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEmissionFactorSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultEmissionFactorSnapshotDesk(store: DefaultEmissionFactorSnapshotStore, clock: Clock): DefaultEmissionFactorSnapshotDesk {
    return DefaultEmissionFactorSnapshotDesk(
        store = store,
        validator = defaultDefaultEmissionFactorSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
