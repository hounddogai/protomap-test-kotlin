package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.clock.Clock

/** Moves tender batches through their life cycle and keeps a journal of what it did. */
data class TenderBatchCoordinator(
    var store: TenderBatchStore,
    var validator: TenderBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender batch $code not found")
        validator.requireValid(item)
        item.moveTo(TenderBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tender batch $code not found")
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

fun newTenderBatchCoordinator(store: TenderBatchStore, clock: Clock): TenderBatchCoordinator {
    return TenderBatchCoordinator(
        store = store,
        validator = defaultTenderBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderBatchEngine(),
    )
}
