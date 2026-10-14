package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary tender quotas through their life cycle and keeps a journal of what it did. */
data class PrimaryTenderQuotaService(
    var store: PrimaryTenderQuotaStore,
    var validator: PrimaryTenderQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTenderQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary tender quota $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTenderQuotaStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tender quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTenderQuotaStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary tender quota $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newPrimaryTenderQuotaService(store: PrimaryTenderQuotaStore, clock: Clock, flags: Registry): PrimaryTenderQuotaService {
    return PrimaryTenderQuotaService(
        store = store,
        validator = defaultPrimaryTenderQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTenderQuotaEngine(flags),
    )
}
