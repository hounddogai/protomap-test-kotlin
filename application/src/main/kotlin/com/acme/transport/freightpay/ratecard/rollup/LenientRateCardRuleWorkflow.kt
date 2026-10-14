package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves lenient rate card rules through their life cycle and keeps a journal of what it did. */
data class LenientRateCardRuleService(
    var store: LenientRateCardRuleStore,
    var validator: LenientRateCardRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient rate card rule $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRateCardRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient rate card rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRateCardRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientRateCardRuleService(store: LenientRateCardRuleStore, clock: Clock): LenientRateCardRuleService {
    return LenientRateCardRuleService(
        store = store,
        validator = defaultLenientRateCardRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
