package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.clock.Clock

/** Moves adaptive ev charge windows through their life cycle and keeps a journal of what it did. */
data class AdaptiveEvChargeWindowDesk(
    var store: AdaptiveEvChargeWindowStore,
    var validator: AdaptiveEvChargeWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive ev charge window $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEvChargeWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive ev charge window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEvChargeWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveEvChargeWindowDesk(store: AdaptiveEvChargeWindowStore, clock: Clock): AdaptiveEvChargeWindowDesk {
    return AdaptiveEvChargeWindowDesk(
        store = store,
        validator = defaultAdaptiveEvChargeWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
