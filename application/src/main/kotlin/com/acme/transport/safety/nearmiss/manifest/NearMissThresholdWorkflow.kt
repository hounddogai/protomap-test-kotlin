package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.safety.clock.Clock

/** Moves near miss thresholds through their life cycle and keeps a journal of what it did. */
data class NearMissThresholdManager(
    var store: NearMissThresholdStore,
    var validator: NearMissThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("near miss threshold $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newNearMissThresholdManager(store: NearMissThresholdStore, clock: Clock): NearMissThresholdManager {
    return NearMissThresholdManager(
        store = store,
        validator = defaultNearMissThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
