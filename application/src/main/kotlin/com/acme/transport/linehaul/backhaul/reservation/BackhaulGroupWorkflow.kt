package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul groups through their life cycle and keeps a journal of what it did. */
data class BackhaulGroupService(
    var store: BackhaulGroupStore,
    var validator: BackhaulGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("backhaul group $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulGroupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulGroupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newBackhaulGroupService(store: BackhaulGroupStore, clock: Clock): BackhaulGroupService {
    return BackhaulGroupService(
        store = store,
        validator = defaultBackhaulGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
