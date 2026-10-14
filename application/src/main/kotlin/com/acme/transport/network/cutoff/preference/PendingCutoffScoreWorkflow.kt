package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves pending cutoff scores through their life cycle and keeps a journal of what it did. */
data class PendingCutoffScoreService(
    var store: PendingCutoffScoreStore,
    var validator: PendingCutoffScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingCutoffScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending cutoff score $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCutoffScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending cutoff score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCutoffScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending cutoff score $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPendingCutoffScoreService(store: PendingCutoffScoreStore, clock: Clock, flags: Registry): PendingCutoffScoreService {
    return PendingCutoffScoreService(
        store = store,
        validator = defaultPendingCutoffScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingCutoffScoreEngine(flags),
    )
}
