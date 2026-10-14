package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.clock.Clock

/** Moves global service plan digests through their life cycle and keeps a journal of what it did. */
data class GlobalServicePlanDigestCoordinator(
    var store: GlobalServicePlanDigestStore,
    var validator: GlobalServicePlanDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global service plan digest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalServicePlanDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global service plan digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalServicePlanDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newGlobalServicePlanDigestCoordinator(store: GlobalServicePlanDigestStore, clock: Clock): GlobalServicePlanDigestCoordinator {
    return GlobalServicePlanDigestCoordinator(
        store = store,
        validator = defaultGlobalServicePlanDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
