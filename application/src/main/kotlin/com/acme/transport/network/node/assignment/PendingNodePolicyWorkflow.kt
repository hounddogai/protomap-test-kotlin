package com.acme.transport.network.node.assignment

import com.acme.transport.network.clock.Clock

/** Moves pending node policies through their life cycle and keeps a journal of what it did. */
data class PendingNodePolicyDesk(
    var store: PendingNodePolicyStore,
    var validator: PendingNodePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending node policy $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNodePolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending node policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNodePolicyStatus.EXPIRED)
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

fun newPendingNodePolicyDesk(store: PendingNodePolicyStore, clock: Clock): PendingNodePolicyDesk {
    return PendingNodePolicyDesk(
        store = store,
        validator = defaultPendingNodePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
