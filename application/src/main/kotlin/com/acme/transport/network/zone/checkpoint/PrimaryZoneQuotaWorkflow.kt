package com.acme.transport.network.zone.checkpoint

import com.acme.transport.network.clock.Clock

/** Moves primary zone quotas through their life cycle and keeps a journal of what it did. */
data class PrimaryZoneQuotaWorkflow(
    var store: PrimaryZoneQuotaStore,
    var validator: PrimaryZoneQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryZoneQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary zone quota $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryZoneQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary zone quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryZoneQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary zone quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPrimaryZoneQuotaWorkflow(store: PrimaryZoneQuotaStore, clock: Clock): PrimaryZoneQuotaWorkflow {
    return PrimaryZoneQuotaWorkflow(
        store = store,
        validator = defaultPrimaryZoneQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryZoneQuotaEngine(),
    )
}
