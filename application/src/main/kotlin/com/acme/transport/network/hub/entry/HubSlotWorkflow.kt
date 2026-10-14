package com.acme.transport.network.hub.entry

import com.acme.transport.network.clock.Clock

/** Moves hub slots through their life cycle and keeps a journal of what it did. */
data class HubSlotService(
    var store: HubSlotStore,
    var validator: HubSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub slot $code not found")
        validator.requireValid(item)
        item.moveTo(HubSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hub slot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newHubSlotService(store: HubSlotStore, clock: Clock): HubSlotService {
    return HubSlotService(
        store = store,
        validator = defaultHubSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubSlotEngine(),
    )
}
