package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.terminal.clock.Clock

/** Moves fallback trailer yard bundles through their life cycle and keeps a journal of what it did. */
data class FallbackTrailerYardBundleService(
    var store: FallbackTrailerYardBundleStore,
    var validator: FallbackTrailerYardBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackTrailerYardBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback trailer yard bundle $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackTrailerYardBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback trailer yard bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackTrailerYardBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback trailer yard bundle $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newFallbackTrailerYardBundleService(store: FallbackTrailerYardBundleStore, clock: Clock): FallbackTrailerYardBundleService {
    return FallbackTrailerYardBundleService(
        store = store,
        validator = defaultFallbackTrailerYardBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackTrailerYardBundleEngine(),
    )
}
