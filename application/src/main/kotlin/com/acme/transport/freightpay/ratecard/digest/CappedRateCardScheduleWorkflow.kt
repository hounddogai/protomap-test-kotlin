package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.clock.Clock

/** Moves capped rate card schedules through their life cycle and keeps a journal of what it did. */
data class CappedRateCardScheduleDesk(
    var store: CappedRateCardScheduleStore,
    var validator: CappedRateCardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped rate card schedule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRateCardScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped rate card schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRateCardScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedRateCardScheduleDesk(store: CappedRateCardScheduleStore, clock: Clock): CappedRateCardScheduleDesk {
    return CappedRateCardScheduleDesk(
        store = store,
        validator = defaultCappedRateCardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
