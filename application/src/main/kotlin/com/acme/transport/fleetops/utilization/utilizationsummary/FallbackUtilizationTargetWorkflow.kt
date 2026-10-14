package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.fleetops.clock.Clock

/** Moves fallback utilization targets through their life cycle and keeps a journal of what it did. */
data class FallbackUtilizationTargetService(
    var store: FallbackUtilizationTargetStore,
    var validator: FallbackUtilizationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback utilization target $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackUtilizationTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback utilization target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackUtilizationTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newFallbackUtilizationTargetService(store: FallbackUtilizationTargetStore, clock: Clock): FallbackUtilizationTargetService {
    return FallbackUtilizationTargetService(
        store = store,
        validator = defaultFallbackUtilizationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
