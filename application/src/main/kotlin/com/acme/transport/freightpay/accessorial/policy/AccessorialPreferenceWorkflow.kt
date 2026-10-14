package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial preferences through their life cycle and keeps a journal of what it did. */
data class AccessorialPreferenceService(
    var store: AccessorialPreferenceStore,
    var validator: AccessorialPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial preference $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialPreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialPreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAccessorialPreferenceService(store: AccessorialPreferenceStore, clock: Clock): AccessorialPreferenceService {
    return AccessorialPreferenceService(
        store = store,
        validator = defaultAccessorialPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
