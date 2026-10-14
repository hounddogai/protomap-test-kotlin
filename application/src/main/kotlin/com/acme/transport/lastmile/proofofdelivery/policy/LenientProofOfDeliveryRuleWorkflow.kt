package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves lenient proof of delivery rules through their life cycle and keeps a journal of what it did.
 */
data class LenientProofOfDeliveryRuleService(
    var store: LenientProofOfDeliveryRuleStore,
    var validator: LenientProofOfDeliveryRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientProofOfDeliveryRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient proof of delivery rule $code not found")
        validator.requireValid(item)
        item.moveTo(LenientProofOfDeliveryRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient proof of delivery rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientProofOfDeliveryRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient proof of delivery rule $code not found")
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

fun newLenientProofOfDeliveryRuleService(store: LenientProofOfDeliveryRuleStore, clock: Clock, flags: Registry): LenientProofOfDeliveryRuleService {
    return LenientProofOfDeliveryRuleService(
        store = store,
        validator = defaultLenientProofOfDeliveryRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientProofOfDeliveryRuleEngine(flags),
    )
}
