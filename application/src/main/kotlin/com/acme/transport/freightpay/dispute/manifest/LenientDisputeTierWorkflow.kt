package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient dispute tiers through their life cycle and keeps a journal of what it did. */
data class LenientDisputeTierManager(
    var store: LenientDisputeTierStore,
    var validator: LenientDisputeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDisputeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient dispute tier $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDisputeTierStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dispute tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDisputeTierStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dispute tier $code not found")
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

fun newLenientDisputeTierManager(store: LenientDisputeTierStore, clock: Clock, flags: Registry): LenientDisputeTierManager {
    return LenientDisputeTierManager(
        store = store,
        validator = defaultLenientDisputeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDisputeTierEngine(flags),
    )
}
