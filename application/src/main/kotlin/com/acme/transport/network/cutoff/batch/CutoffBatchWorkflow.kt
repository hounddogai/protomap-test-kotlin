package com.acme.transport.network.cutoff.batch

import com.acme.transport.network.clock.Clock

/** Moves cutoff batches through their life cycle and keeps a journal of what it did. */
data class CutoffBatchService(
    var store: CutoffBatchStore,
    var validator: CutoffBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cutoff batch $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCutoffBatchService(store: CutoffBatchStore, clock: Clock): CutoffBatchService {
    return CutoffBatchService(
        store = store,
        validator = defaultCutoffBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
