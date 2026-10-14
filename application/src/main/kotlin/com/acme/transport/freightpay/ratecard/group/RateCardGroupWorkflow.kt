package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card groups through their life cycle and keeps a journal of what it did. */
data class RateCardGroupManager(
    var store: RateCardGroupStore,
    var validator: RateCardGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rate card group $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRateCardGroupManager(store: RateCardGroupStore, clock: Clock): RateCardGroupManager {
    return RateCardGroupManager(
        store = store,
        validator = defaultRateCardGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
