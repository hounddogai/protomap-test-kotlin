package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default redelivery bundles through their life cycle and keeps a journal of what it did. */
data class DefaultRedeliveryBundleDesk(
    var store: DefaultRedeliveryBundleStore,
    var validator: DefaultRedeliveryBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultRedeliveryBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default redelivery bundle $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRedeliveryBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default redelivery bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRedeliveryBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default redelivery bundle $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultRedeliveryBundleDesk(store: DefaultRedeliveryBundleStore, clock: Clock, flags: Registry): DefaultRedeliveryBundleDesk {
    return DefaultRedeliveryBundleDesk(
        store = store,
        validator = defaultDefaultRedeliveryBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultRedeliveryBundleEngine(flags),
    )
}
