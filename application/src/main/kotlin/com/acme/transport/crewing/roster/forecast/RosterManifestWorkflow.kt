package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.clock.Clock

/** Moves roster manifests through their life cycle and keeps a journal of what it did. */
data class RosterManifestDesk(
    var store: RosterManifestStore,
    var validator: RosterManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("roster manifest $code not found")
        validator.requireValid(item)
        item.moveTo(RosterManifestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("roster manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterManifestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("roster manifest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRosterManifestDesk(store: RosterManifestStore, clock: Clock): RosterManifestDesk {
    return RosterManifestDesk(
        store = store,
        validator = defaultRosterManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterManifestEngine(),
    )
}
