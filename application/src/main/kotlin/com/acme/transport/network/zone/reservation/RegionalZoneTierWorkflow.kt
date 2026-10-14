package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves regional zone tiers through their life cycle and keeps a journal of what it did. */
data class RegionalZoneTierService(
    var store: RegionalZoneTierStore,
    var validator: RegionalZoneTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalZoneTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional zone tier $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalZoneTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional zone tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalZoneTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional zone tier $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newRegionalZoneTierService(store: RegionalZoneTierStore, clock: Clock, flags: Registry): RegionalZoneTierService {
    return RegionalZoneTierService(
        store = store,
        validator = defaultRegionalZoneTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalZoneTierEngine(flags),
    )
}
