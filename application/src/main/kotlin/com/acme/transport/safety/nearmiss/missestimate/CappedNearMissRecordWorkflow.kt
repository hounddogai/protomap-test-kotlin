package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.safety.clock.Clock

/** Moves capped near miss records through their life cycle and keeps a journal of what it did. */
data class CappedNearMissRecordCoordinator(
    var store: CappedNearMissRecordStore,
    var validator: CappedNearMissRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped near miss record $code not found")
        validator.requireValid(item)
        item.moveTo(CappedNearMissRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped near miss record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedNearMissRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newCappedNearMissRecordCoordinator(store: CappedNearMissRecordStore, clock: Clock): CappedNearMissRecordCoordinator {
    return CappedNearMissRecordCoordinator(
        store = store,
        validator = defaultCappedNearMissRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
