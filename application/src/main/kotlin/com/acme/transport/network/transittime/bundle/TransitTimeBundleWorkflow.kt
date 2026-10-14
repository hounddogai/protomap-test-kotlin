package com.acme.transport.network.transittime.bundle

import com.acme.transport.network.clock.Clock

/** Moves transit time bundles through their life cycle and keeps a journal of what it did. */
data class TransitTimeBundleManager(
    var store: TransitTimeBundleStore,
    var validator: TransitTimeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("transit time bundle $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeBundleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeBundleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("transit time bundle $code not found")
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

fun newTransitTimeBundleManager(store: TransitTimeBundleStore, clock: Clock): TransitTimeBundleManager {
    return TransitTimeBundleManager(
        store = store,
        validator = defaultTransitTimeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeBundleEngine(),
    )
}
