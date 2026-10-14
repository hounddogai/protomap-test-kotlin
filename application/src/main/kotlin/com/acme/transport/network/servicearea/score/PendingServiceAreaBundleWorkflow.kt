package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves pending service area bundles through their life cycle and keeps a journal of what it did. */
data class PendingServiceAreaBundleDesk(
    var store: PendingServiceAreaBundleStore,
    var validator: PendingServiceAreaBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingServiceAreaBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending service area bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PendingServiceAreaBundleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending service area bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingServiceAreaBundleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending service area bundle $code not found")
        val result = engine.evaluate(item)
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

fun newPendingServiceAreaBundleDesk(store: PendingServiceAreaBundleStore, clock: Clock, flags: Registry): PendingServiceAreaBundleDesk {
    return PendingServiceAreaBundleDesk(
        store = store,
        validator = defaultPendingServiceAreaBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingServiceAreaBundleEngine(flags),
    )
}
