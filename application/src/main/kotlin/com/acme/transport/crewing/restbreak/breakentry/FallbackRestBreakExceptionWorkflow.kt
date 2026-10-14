package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback rest break exceptions through their life cycle and keeps a journal of what it did.
 */
data class FallbackRestBreakExceptionManager(
    var store: FallbackRestBreakExceptionStore,
    var validator: FallbackRestBreakExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRestBreakExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback rest break exception $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRestBreakExceptionStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rest break exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRestBreakExceptionStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback rest break exception $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFallbackRestBreakExceptionManager(store: FallbackRestBreakExceptionStore, clock: Clock, flags: Registry): FallbackRestBreakExceptionManager {
    return FallbackRestBreakExceptionManager(
        store = store,
        validator = defaultFallbackRestBreakExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRestBreakExceptionEngine(flags),
    )
}
