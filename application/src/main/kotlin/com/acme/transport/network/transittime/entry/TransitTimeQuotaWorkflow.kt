package com.acme.transport.network.transittime.entry

import com.acme.transport.network.clock.Clock

/** Moves transit time quotas through their life cycle and keeps a journal of what it did. */
data class TransitTimeQuotaService(
    var store: TransitTimeQuotaStore,
    var validator: TransitTimeQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("transit time quota $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("transit time quota $code not found")
        val result = engine.price(item)
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

fun newTransitTimeQuotaService(store: TransitTimeQuotaStore, clock: Clock): TransitTimeQuotaService {
    return TransitTimeQuotaService(
        store = store,
        validator = defaultTransitTimeQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeQuotaEngine(),
    )
}
