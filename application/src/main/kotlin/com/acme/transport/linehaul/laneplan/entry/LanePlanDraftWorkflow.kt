package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan drafts through their life cycle and keeps a journal of what it did. */
data class LanePlanDraftCoordinator(
    var store: LanePlanDraftStore,
    var validator: LanePlanDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lane plan draft $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanDraftStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanDraftStatus.WITHDRAWN)
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

fun newLanePlanDraftCoordinator(store: LanePlanDraftStore, clock: Clock): LanePlanDraftCoordinator {
    return LanePlanDraftCoordinator(
        store = store,
        validator = defaultLanePlanDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
