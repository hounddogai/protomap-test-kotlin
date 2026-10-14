package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.clock.Clock

/** Moves capped utilization claims through their life cycle and keeps a journal of what it did. */
data class CappedUtilizationClaimDesk(
    var store: CappedUtilizationClaimStore,
    var validator: CappedUtilizationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped utilization claim $code not found")
        validator.requireValid(item)
        item.moveTo(CappedUtilizationClaimStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped utilization claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedUtilizationClaimStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newCappedUtilizationClaimDesk(store: CappedUtilizationClaimStore, clock: Clock): CappedUtilizationClaimDesk {
    return CappedUtilizationClaimDesk(
        store = store,
        validator = defaultCappedUtilizationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
