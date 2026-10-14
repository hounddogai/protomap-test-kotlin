package com.acme.transport.network.node.nodedraft

import com.acme.transport.network.clock.Clock

/** Moves rolling node drafts through their life cycle and keeps a journal of what it did. */
data class RollingNodeDraftManager(
    var store: RollingNodeDraftStore,
    var validator: RollingNodeDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling node draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingNodeDraftStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling node draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingNodeDraftStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newRollingNodeDraftManager(store: RollingNodeDraftStore, clock: Clock): RollingNodeDraftManager {
    return RollingNodeDraftManager(
        store = store,
        validator = defaultRollingNodeDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
