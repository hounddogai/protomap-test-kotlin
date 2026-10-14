package com.acme.transport.network.zone.quota

import com.acme.transport.network.clock.Clock

/** Moves zone quotas through their life cycle and keeps a journal of what it did. */
data class ZoneQuotaDesk(
    var store: ZoneQuotaStore,
    var validator: ZoneQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("zone quota $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("zone quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone quota $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newZoneQuotaDesk(store: ZoneQuotaStore, clock: Clock): ZoneQuotaDesk {
    return ZoneQuotaDesk(
        store = store,
        validator = defaultZoneQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneQuotaEngine(),
    )
}
