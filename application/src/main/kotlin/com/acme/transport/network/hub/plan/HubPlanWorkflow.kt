package com.acme.transport.network.hub.plan

import com.acme.transport.network.clock.Clock

/** Moves hub plans through their life cycle and keeps a journal of what it did. */
data class HubPlanDesk(
    var store: HubPlanStore,
    var validator: HubPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hub plan $code not found")
        validator.requireValid(item)
        item.moveTo(HubPlanStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubPlanStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hub plan $code not found")
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

fun newHubPlanDesk(store: HubPlanStore, clock: Clock): HubPlanDesk {
    return HubPlanDesk(
        store = store,
        validator = defaultHubPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubPlanEngine(),
    )
}
