package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves pending node bundles through their life cycle and keeps a journal of what it did. */
data class PendingNodeBundleService(
    var store: PendingNodeBundleStore,
    var validator: PendingNodeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingNodeBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending node bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNodeBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending node bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNodeBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending node bundle $code not found")
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

fun newPendingNodeBundleService(store: PendingNodeBundleStore, clock: Clock, flags: Registry): PendingNodeBundleService {
    return PendingNodeBundleService(
        store = store,
        validator = defaultPendingNodeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingNodeBundleEngine(flags),
    )
}
