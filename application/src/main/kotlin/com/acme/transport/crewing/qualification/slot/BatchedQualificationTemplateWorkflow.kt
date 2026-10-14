package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.clock.Clock

/**
 * Moves batched qualification templates through their life cycle and keeps a journal of what it did.
 */
data class BatchedQualificationTemplateCoordinator(
    var store: BatchedQualificationTemplateStore,
    var validator: BatchedQualificationTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched qualification template $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedQualificationTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched qualification template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedQualificationTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newBatchedQualificationTemplateCoordinator(store: BatchedQualificationTemplateStore, clock: Clock): BatchedQualificationTemplateCoordinator {
    return BatchedQualificationTemplateCoordinator(
        store = store,
        validator = defaultBatchedQualificationTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
