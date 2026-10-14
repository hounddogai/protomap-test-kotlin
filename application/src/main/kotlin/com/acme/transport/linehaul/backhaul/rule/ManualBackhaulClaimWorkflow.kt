package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.linehaul.clock.Clock

/** Moves manual backhaul claims through their life cycle and keeps a journal of what it did. */
data class ManualBackhaulClaimService(
    var store: ManualBackhaulClaimStore,
    var validator: ManualBackhaulClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual backhaul claim $code not found")
        validator.requireValid(item)
        item.moveTo(ManualBackhaulClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual backhaul claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualBackhaulClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newManualBackhaulClaimService(store: ManualBackhaulClaimStore, clock: Clock): ManualBackhaulClaimService {
    return ManualBackhaulClaimService(
        store = store,
        validator = defaultManualBackhaulClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
