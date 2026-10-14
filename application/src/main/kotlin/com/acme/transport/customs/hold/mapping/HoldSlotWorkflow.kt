package com.acme.transport.customs.hold.mapping

import com.acme.transport.customs.clock.Clock

/** Moves hold slots through their life cycle and keeps a journal of what it did. */
data class HoldSlotService(
    var store: HoldSlotStore,
    var validator: HoldSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold slot $code not found")
        validator.requireValid(item)
        item.moveTo(HoldSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hold slot $code not found")
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

fun newHoldSlotService(store: HoldSlotStore, clock: Clock): HoldSlotService {
    return HoldSlotService(
        store = store,
        validator = defaultHoldSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldSlotEngine(),
    )
}
