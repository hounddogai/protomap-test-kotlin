package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub overrides through their life cycle and keeps a journal of what it did. */
data class HubOverrideDesk(
    var store: HubOverrideStore,
    var validator: HubOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hub override $code not found")
        validator.requireValid(item)
        item.moveTo(HubOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("hub override $code not found")
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

fun newHubOverrideDesk(store: HubOverrideStore, clock: Clock, flags: Registry): HubOverrideDesk {
    return HubOverrideDesk(
        store = store,
        validator = defaultHubOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubOverrideEngine(flags),
    )
}
