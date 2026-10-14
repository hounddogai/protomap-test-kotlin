package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient rate card templates through their life cycle and keeps a journal of what it did. */
data class LenientRateCardTemplateDesk(
    var store: LenientRateCardTemplateStore,
    var validator: LenientRateCardTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientRateCardTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient rate card template $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRateCardTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient rate card template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRateCardTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient rate card template $code not found")
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

fun newLenientRateCardTemplateDesk(store: LenientRateCardTemplateStore, clock: Clock, flags: Registry): LenientRateCardTemplateDesk {
    return LenientRateCardTemplateDesk(
        store = store,
        validator = defaultLenientRateCardTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientRateCardTemplateEngine(flags),
    )
}
