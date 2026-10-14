package com.acme.transport.network.hub.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub rollups through their life cycle and keeps a journal of what it did. */
data class HubRollupManager(
    var store: HubRollupStore,
    var validator: HubRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub rollup $code not found")
        validator.requireValid(item)
        item.moveTo(HubRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hub rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("hub rollup $code not found")
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

fun newHubRollupManager(store: HubRollupStore, clock: Clock, flags: Registry): HubRollupManager {
    return HubRollupManager(
        store = store,
        validator = defaultHubRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubRollupEngine(flags),
    )
}
