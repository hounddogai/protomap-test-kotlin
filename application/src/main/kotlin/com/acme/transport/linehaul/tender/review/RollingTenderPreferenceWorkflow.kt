package com.acme.transport.linehaul.tender.review

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling tender preferences through their life cycle and keeps a journal of what it did. */
data class RollingTenderPreferenceService(
    var store: RollingTenderPreferenceStore,
    var validator: RollingTenderPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling tender preference $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTenderPreferenceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling tender preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTenderPreferenceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRollingTenderPreferenceService(store: RollingTenderPreferenceStore, clock: Clock): RollingTenderPreferenceService {
    return RollingTenderPreferenceService(
        store = store,
        validator = defaultRollingTenderPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
