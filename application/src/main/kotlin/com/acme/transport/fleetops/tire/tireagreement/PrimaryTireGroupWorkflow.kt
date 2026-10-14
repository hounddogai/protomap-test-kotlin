package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.clock.Clock

/** Moves primary tire groups through their life cycle and keeps a journal of what it did. */
data class PrimaryTireGroupDesk(
    var store: PrimaryTireGroupStore,
    var validator: PrimaryTireGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary tire group $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTireGroupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tire group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTireGroupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPrimaryTireGroupDesk(store: PrimaryTireGroupStore, clock: Clock): PrimaryTireGroupDesk {
    return PrimaryTireGroupDesk(
        store = store,
        validator = defaultPrimaryTireGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
