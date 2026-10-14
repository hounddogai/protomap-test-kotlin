package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.network.clock.Clock

/** Moves cutoff exceptions through their life cycle and keeps a journal of what it did. */
data class CutoffExceptionDesk(
    var store: CutoffExceptionStore,
    var validator: CutoffExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cutoff exception $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffExceptionStatus.FAILED)
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

fun newCutoffExceptionDesk(store: CutoffExceptionStore, clock: Clock): CutoffExceptionDesk {
    return CutoffExceptionDesk(
        store = store,
        validator = defaultCutoffExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
