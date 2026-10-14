package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.network.clock.Clock

/** Moves pending cutoff segments through their life cycle and keeps a journal of what it did. */
data class PendingCutoffSegmentManager(
    var store: PendingCutoffSegmentStore,
    var validator: PendingCutoffSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingCutoffSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending cutoff segment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCutoffSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending cutoff segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCutoffSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending cutoff segment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingCutoffSegmentManager(store: PendingCutoffSegmentStore, clock: Clock): PendingCutoffSegmentManager {
    return PendingCutoffSegmentManager(
        store = store,
        validator = defaultPendingCutoffSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingCutoffSegmentEngine(),
    )
}
