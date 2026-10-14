package com.acme.transport.customs.hold.policy

import com.acme.transport.customs.clock.Clock

/** Moves hold claims through their life cycle and keeps a journal of what it did. */
data class HoldClaimManager(
    var store: HoldClaimStore,
    var validator: HoldClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hold claim $code not found")
        validator.requireValid(item)
        item.moveTo(HoldClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hold claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newHoldClaimManager(store: HoldClaimStore, clock: Clock): HoldClaimManager {
    return HoldClaimManager(
        store = store,
        validator = defaultHoldClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
