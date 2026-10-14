package com.acme.transport.network.hub.overrideitem

import com.acme.transport.network.clock.Clock

/** Moves hub schedules through their life cycle and keeps a journal of what it did. */
data class HubScheduleCoordinator(
    var store: HubScheduleStore,
    var validator: HubScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub schedule $code not found")
        validator.requireValid(item)
        item.moveTo(HubScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("hub schedule $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newHubScheduleCoordinator(store: HubScheduleStore, clock: Clock): HubScheduleCoordinator {
    return HubScheduleCoordinator(
        store = store,
        validator = defaultHubScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubScheduleEngine(),
    )
}
