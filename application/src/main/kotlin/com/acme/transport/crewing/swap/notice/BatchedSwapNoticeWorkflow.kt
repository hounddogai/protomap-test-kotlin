package com.acme.transport.crewing.swap.notice

import com.acme.transport.crewing.clock.Clock

/** Moves batched swap notices through their life cycle and keeps a journal of what it did. */
data class BatchedSwapNoticeManager(
    var store: BatchedSwapNoticeStore,
    var validator: BatchedSwapNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedSwapNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched swap notice $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedSwapNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched swap notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedSwapNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched swap notice $code not found")
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

fun newBatchedSwapNoticeManager(store: BatchedSwapNoticeStore, clock: Clock): BatchedSwapNoticeManager {
    return BatchedSwapNoticeManager(
        store = store,
        validator = defaultBatchedSwapNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedSwapNoticeEngine(),
    )
}
