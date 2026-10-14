package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.customs.clock.Clock

/** Moves global hold batches through their life cycle and keeps a journal of what it did. */
data class GlobalHoldBatchDesk(
    var store: GlobalHoldBatchStore,
    var validator: GlobalHoldBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global hold batch $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalHoldBatchStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global hold batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalHoldBatchStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newGlobalHoldBatchDesk(store: GlobalHoldBatchStore, clock: Clock): GlobalHoldBatchDesk {
    return GlobalHoldBatchDesk(
        store = store,
        validator = defaultGlobalHoldBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
