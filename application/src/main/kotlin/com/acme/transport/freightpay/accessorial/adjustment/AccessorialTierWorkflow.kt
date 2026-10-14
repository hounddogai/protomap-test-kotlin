package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial tiers through their life cycle and keeps a journal of what it did. */
data class AccessorialTierDesk(
    var store: AccessorialTierStore,
    var validator: AccessorialTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial tier $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialTierStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialTierStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAccessorialTierDesk(store: AccessorialTierStore, clock: Clock): AccessorialTierDesk {
    return AccessorialTierDesk(
        store = store,
        validator = defaultAccessorialTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
