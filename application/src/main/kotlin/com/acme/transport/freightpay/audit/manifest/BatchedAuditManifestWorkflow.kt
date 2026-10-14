package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.clock.Clock

/** Moves batched audit manifests through their life cycle and keeps a journal of what it did. */
data class BatchedAuditManifestDesk(
    var store: BatchedAuditManifestStore,
    var validator: BatchedAuditManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched audit manifest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAuditManifestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched audit manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAuditManifestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newBatchedAuditManifestDesk(store: BatchedAuditManifestStore, clock: Clock): BatchedAuditManifestDesk {
    return BatchedAuditManifestDesk(
        store = store,
        validator = defaultBatchedAuditManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
