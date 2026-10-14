package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback swap groups through their life cycle and keeps a journal of what it did. */
data class FallbackSwapGroupService(
    var store: FallbackSwapGroupStore,
    var validator: FallbackSwapGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackSwapGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback swap group $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSwapGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback swap group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSwapGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback swap group $code not found")
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

fun newFallbackSwapGroupService(store: FallbackSwapGroupStore, clock: Clock, flags: Registry): FallbackSwapGroupService {
    return FallbackSwapGroupService(
        store = store,
        validator = defaultFallbackSwapGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackSwapGroupEngine(flags),
    )
}
