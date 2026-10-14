package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.clock.Clock

/** Moves batched declaration manifests through their life cycle and keeps a journal of what it did. */
data class BatchedDeclarationManifestWorkflow(
    var store: BatchedDeclarationManifestStore,
    var validator: BatchedDeclarationManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDeclarationManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched declaration manifest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDeclarationManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched declaration manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDeclarationManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched declaration manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newBatchedDeclarationManifestWorkflow(store: BatchedDeclarationManifestStore, clock: Clock): BatchedDeclarationManifestWorkflow {
    return BatchedDeclarationManifestWorkflow(
        store = store,
        validator = defaultBatchedDeclarationManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDeclarationManifestEngine(),
    )
}
