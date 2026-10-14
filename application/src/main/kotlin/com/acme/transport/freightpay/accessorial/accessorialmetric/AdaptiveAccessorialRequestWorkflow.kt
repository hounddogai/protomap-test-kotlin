package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.Clock

/** Moves adaptive accessorial requests through their life cycle and keeps a journal of what it did. */
data class AdaptiveAccessorialRequestCoordinator(
    var store: AdaptiveAccessorialRequestStore,
    var validator: AdaptiveAccessorialRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive accessorial request $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAccessorialRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive accessorial request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAccessorialRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAdaptiveAccessorialRequestCoordinator(store: AdaptiveAccessorialRequestStore, clock: Clock): AdaptiveAccessorialRequestCoordinator {
    return AdaptiveAccessorialRequestCoordinator(
        store = store,
        validator = defaultAdaptiveAccessorialRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
