package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub adjustments through their life cycle and keeps a journal of what it did. */
data class HubAdjustmentDesk(
    var store: HubAdjustmentStore,
    var validator: HubAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(HubAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hub adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hub adjustment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newHubAdjustmentDesk(store: HubAdjustmentStore, clock: Clock, flags: Registry): HubAdjustmentDesk {
    return HubAdjustmentDesk(
        store = store,
        validator = defaultHubAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubAdjustmentEngine(flags),
    )
}
