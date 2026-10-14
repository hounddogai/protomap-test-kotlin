package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary trailer bundles through their life cycle and keeps a journal of what it did. */
data class PrimaryTrailerBundleWorkflow(
    var store: PrimaryTrailerBundleStore,
    var validator: PrimaryTrailerBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTrailerBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary trailer bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTrailerBundleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary trailer bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTrailerBundleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary trailer bundle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPrimaryTrailerBundleWorkflow(store: PrimaryTrailerBundleStore, clock: Clock, flags: Registry): PrimaryTrailerBundleWorkflow {
    return PrimaryTrailerBundleWorkflow(
        store = store,
        validator = defaultPrimaryTrailerBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTrailerBundleEngine(flags),
    )
}
