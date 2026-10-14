package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan snapshots through their life cycle and keeps a journal of what it did. */
data class ServicePlanSnapshotDesk(
    var store: ServicePlanSnapshotStore,
    var validator: ServicePlanSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service plan snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newServicePlanSnapshotDesk(store: ServicePlanSnapshotStore, clock: Clock): ServicePlanSnapshotDesk {
    return ServicePlanSnapshotDesk(
        store = store,
        validator = defaultServicePlanSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
