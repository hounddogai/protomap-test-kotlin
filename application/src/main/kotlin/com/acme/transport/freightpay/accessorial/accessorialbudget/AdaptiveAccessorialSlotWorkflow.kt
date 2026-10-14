package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.clock.Clock

/** Moves adaptive accessorial slots through their life cycle and keeps a journal of what it did. */
data class AdaptiveAccessorialSlotCoordinator(
    var store: AdaptiveAccessorialSlotStore,
    var validator: AdaptiveAccessorialSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive accessorial slot $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAccessorialSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive accessorial slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAccessorialSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveAccessorialSlotCoordinator(store: AdaptiveAccessorialSlotStore, clock: Clock): AdaptiveAccessorialSlotCoordinator {
    return AdaptiveAccessorialSlotCoordinator(
        store = store,
        validator = defaultAdaptiveAccessorialSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
