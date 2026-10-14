package com.acme.transport.linehaul.tender.metric

import com.acme.transport.linehaul.clock.Clock

/** Moves pending tender scores through their life cycle and keeps a journal of what it did. */
data class PendingTenderScoreManager(
    var store: PendingTenderScoreStore,
    var validator: PendingTenderScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTenderScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending tender score $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTenderScoreStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tender score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTenderScoreStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending tender score $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newPendingTenderScoreManager(store: PendingTenderScoreStore, clock: Clock): PendingTenderScoreManager {
    return PendingTenderScoreManager(
        store = store,
        validator = defaultPendingTenderScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTenderScoreEngine(),
    )
}
