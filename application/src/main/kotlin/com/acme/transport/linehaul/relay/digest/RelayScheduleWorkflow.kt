package com.acme.transport.linehaul.relay.digest

import com.acme.transport.linehaul.clock.Clock

/** Moves relay schedules through their life cycle and keeps a journal of what it did. */
data class RelayScheduleService(
    var store: RelayScheduleStore,
    var validator: RelayScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay schedule $code not found")
        validator.requireValid(item)
        item.moveTo(RelayScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("relay schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay schedule $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRelayScheduleService(store: RelayScheduleStore, clock: Clock): RelayScheduleService {
    return RelayScheduleService(
        store = store,
        validator = defaultRelayScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayScheduleEngine(),
    )
}
