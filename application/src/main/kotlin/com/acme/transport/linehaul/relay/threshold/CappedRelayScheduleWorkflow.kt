package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves capped relay schedules through their life cycle and keeps a journal of what it did. */
data class CappedRelayScheduleManager(
    var store: CappedRelayScheduleStore,
    var validator: CappedRelayScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedRelayScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped relay schedule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRelayScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped relay schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRelayScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped relay schedule $code not found")
        val result = engine.compute(item)
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

fun newCappedRelayScheduleManager(store: CappedRelayScheduleStore, clock: Clock): CappedRelayScheduleManager {
    return CappedRelayScheduleManager(
        store = store,
        validator = defaultCappedRelayScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedRelayScheduleEngine(),
    )
}
