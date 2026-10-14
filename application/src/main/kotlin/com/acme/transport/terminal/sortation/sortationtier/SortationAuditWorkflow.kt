package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.terminal.clock.Clock

/** Moves sortation audits through their life cycle and keeps a journal of what it did. */
data class SortationAuditCoordinator(
    var store: SortationAuditStore,
    var validator: SortationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("sortation audit $code not found")
        validator.requireValid(item)
        item.moveTo(SortationAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newSortationAuditCoordinator(store: SortationAuditStore, clock: Clock): SortationAuditCoordinator {
    return SortationAuditCoordinator(
        store = store,
        validator = defaultSortationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
