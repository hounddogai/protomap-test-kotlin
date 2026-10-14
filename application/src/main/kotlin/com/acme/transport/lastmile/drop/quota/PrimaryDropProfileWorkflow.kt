package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves primary drop profiles through their life cycle and keeps a journal of what it did. */
data class PrimaryDropProfileDesk(
    var store: PrimaryDropProfileStore,
    var validator: PrimaryDropProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDropProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary drop profile $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDropProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary drop profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDropProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary drop profile $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryDropProfileDesk(store: PrimaryDropProfileStore, clock: Clock, flags: Registry): PrimaryDropProfileDesk {
    return PrimaryDropProfileDesk(
        store = store,
        validator = defaultPrimaryDropProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDropProfileEngine(flags),
    )
}
