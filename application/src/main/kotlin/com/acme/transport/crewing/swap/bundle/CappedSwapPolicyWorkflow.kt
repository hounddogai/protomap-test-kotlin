package com.acme.transport.crewing.swap.bundle

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped swap policies through their life cycle and keeps a journal of what it did. */
data class CappedSwapPolicyService(
    var store: CappedSwapPolicyStore,
    var validator: CappedSwapPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedSwapPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped swap policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedSwapPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped swap policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedSwapPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped swap policy $code not found")
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

fun newCappedSwapPolicyService(store: CappedSwapPolicyStore, clock: Clock, flags: Registry): CappedSwapPolicyService {
    return CappedSwapPolicyService(
        store = store,
        validator = defaultCappedSwapPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedSwapPolicyEngine(flags),
    )
}
