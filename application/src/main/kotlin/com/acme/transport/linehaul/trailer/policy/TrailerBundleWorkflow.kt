package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer bundles through their life cycle and keeps a journal of what it did. */
data class TrailerBundleDesk(
    var store: TrailerBundleStore,
    var validator: TrailerBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer bundle $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("trailer bundle $code not found")
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

fun newTrailerBundleDesk(store: TrailerBundleStore, clock: Clock, flags: Registry): TrailerBundleDesk {
    return TrailerBundleDesk(
        store = store,
        validator = defaultTrailerBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerBundleEngine(flags),
    )
}
