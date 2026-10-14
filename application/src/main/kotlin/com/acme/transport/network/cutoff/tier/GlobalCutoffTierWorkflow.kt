package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global cutoff tiers through their life cycle and keeps a journal of what it did. */
data class GlobalCutoffTierDesk(
    var store: GlobalCutoffTierStore,
    var validator: GlobalCutoffTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCutoffTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global cutoff tier $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCutoffTierStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global cutoff tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCutoffTierStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global cutoff tier $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newGlobalCutoffTierDesk(store: GlobalCutoffTierStore, clock: Clock, flags: Registry): GlobalCutoffTierDesk {
    return GlobalCutoffTierDesk(
        store = store,
        validator = defaultGlobalCutoffTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCutoffTierEngine(flags),
    )
}
