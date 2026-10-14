package com.acme.transport.network.hub.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub baselines through their life cycle and keeps a journal of what it did. */
data class HubBaselineService(
    var store: HubBaselineStore,
    var validator: HubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(HubBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("hub baseline $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newHubBaselineService(store: HubBaselineStore, clock: Clock, flags: Registry): HubBaselineService {
    return HubBaselineService(
        store = store,
        validator = defaultHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubBaselineEngine(flags),
    )
}
