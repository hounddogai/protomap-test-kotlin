package com.acme.transport.network.cutoff.estimate

import com.acme.transport.network.clock.Clock

/** Moves primary cutoff batches through their life cycle and keeps a journal of what it did. */
data class PrimaryCutoffBatchManager(
    var store: PrimaryCutoffBatchStore,
    var validator: PrimaryCutoffBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCutoffBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary cutoff batch $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCutoffBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary cutoff batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCutoffBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary cutoff batch $code not found")
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

fun newPrimaryCutoffBatchManager(store: PrimaryCutoffBatchStore, clock: Clock): PrimaryCutoffBatchManager {
    return PrimaryCutoffBatchManager(
        store = store,
        validator = defaultPrimaryCutoffBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCutoffBatchEngine(),
    )
}
