package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.network.clock.Clock

/** Moves regional hub snapshots through their life cycle and keeps a journal of what it did. */
data class RegionalHubSnapshotDesk(
    var store: RegionalHubSnapshotStore,
    var validator: RegionalHubSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional hub snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalHubSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional hub snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalHubSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRegionalHubSnapshotDesk(store: RegionalHubSnapshotStore, clock: Clock): RegionalHubSnapshotDesk {
    return RegionalHubSnapshotDesk(
        store = store,
        validator = defaultRegionalHubSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
