package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient dispute templates through their life cycle and keeps a journal of what it did. */
data class LenientDisputeTemplateManager(
    var store: LenientDisputeTemplateStore,
    var validator: LenientDisputeTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDisputeTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient dispute template $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDisputeTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dispute template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDisputeTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dispute template $code not found")
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

fun newLenientDisputeTemplateManager(store: LenientDisputeTemplateStore, clock: Clock, flags: Registry): LenientDisputeTemplateManager {
    return LenientDisputeTemplateManager(
        store = store,
        validator = defaultLenientDisputeTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDisputeTemplateEngine(flags),
    )
}
