package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.lastmile.clock.Clock

/** Moves global attempt claims through their life cycle and keeps a journal of what it did. */
data class GlobalAttemptClaimWorkflow(
    var store: GlobalAttemptClaimStore,
    var validator: GlobalAttemptClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global attempt claim $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAttemptClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global attempt claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAttemptClaimStatus.FAILED)
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

fun newGlobalAttemptClaimWorkflow(store: GlobalAttemptClaimStore, clock: Clock): GlobalAttemptClaimWorkflow {
    return GlobalAttemptClaimWorkflow(
        store = store,
        validator = defaultGlobalAttemptClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
