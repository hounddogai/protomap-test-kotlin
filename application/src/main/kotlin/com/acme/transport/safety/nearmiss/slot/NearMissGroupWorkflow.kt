package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.safety.clock.Clock

/** Moves near miss groups through their life cycle and keeps a journal of what it did. */
data class NearMissGroupDesk(
    var store: NearMissGroupStore,
    var validator: NearMissGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("near miss group $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newNearMissGroupDesk(store: NearMissGroupStore, clock: Clock): NearMissGroupDesk {
    return NearMissGroupDesk(
        store = store,
        validator = defaultNearMissGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
