package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return manifests through their life cycle and keeps a journal of what it did. */
data class LeaseReturnManifestDesk(
    var store: LeaseReturnManifestStore,
    var validator: LeaseReturnManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lease return manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLeaseReturnManifestDesk(store: LeaseReturnManifestStore, clock: Clock): LeaseReturnManifestDesk {
    return LeaseReturnManifestDesk(
        store = store,
        validator = defaultLeaseReturnManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
