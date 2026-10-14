package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.safety.clock.Clock

/** Moves tiered near miss profiles through their life cycle and keeps a journal of what it did. */
data class TieredNearMissProfileManager(
    var store: TieredNearMissProfileStore,
    var validator: TieredNearMissProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered near miss profile $code not found")
        validator.requireValid(item)
        item.moveTo(TieredNearMissProfileStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered near miss profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredNearMissProfileStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTieredNearMissProfileManager(store: TieredNearMissProfileStore, clock: Clock): TieredNearMissProfileManager {
    return TieredNearMissProfileManager(
        store = store,
        validator = defaultTieredNearMissProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
