package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.safety.clock.Clock

/** Moves near miss scores through their life cycle and keeps a journal of what it did. */
data class NearMissScoreCoordinator(
    var store: NearMissScoreStore,
    var validator: NearMissScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("near miss score $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newNearMissScoreCoordinator(store: NearMissScoreStore, clock: Clock): NearMissScoreCoordinator {
    return NearMissScoreCoordinator(
        store = store,
        validator = defaultNearMissScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
