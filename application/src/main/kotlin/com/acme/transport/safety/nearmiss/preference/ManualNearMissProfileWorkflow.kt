package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.clock.Clock

/** Moves manual near miss profiles through their life cycle and keeps a journal of what it did. */
data class ManualNearMissProfileManager(
    var store: ManualNearMissProfileStore,
    var validator: ManualNearMissProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual near miss profile $code not found")
        validator.requireValid(item)
        item.moveTo(ManualNearMissProfileStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual near miss profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualNearMissProfileStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newManualNearMissProfileManager(store: ManualNearMissProfileStore, clock: Clock): ManualNearMissProfileManager {
    return ManualNearMissProfileManager(
        store = store,
        validator = defaultManualNearMissProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
