package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves batched emission factor manifests through their life cycle and keeps a journal of what it did.
 */
data class BatchedEmissionFactorManifestDesk(
    var store: BatchedEmissionFactorManifestStore,
    var validator: BatchedEmissionFactorManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedEmissionFactorManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched emission factor manifest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedEmissionFactorManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched emission factor manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedEmissionFactorManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched emission factor manifest $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newBatchedEmissionFactorManifestDesk(store: BatchedEmissionFactorManifestStore, clock: Clock, flags: Registry): BatchedEmissionFactorManifestDesk {
    return BatchedEmissionFactorManifestDesk(
        store = store,
        validator = defaultBatchedEmissionFactorManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedEmissionFactorManifestEngine(flags),
    )
}
