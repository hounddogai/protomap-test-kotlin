package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.terminal.clock.Clock

/** Moves rolling dock door templates through their life cycle and keeps a journal of what it did. */
data class RollingDockDoorTemplateCoordinator(
    var store: RollingDockDoorTemplateStore,
    var validator: RollingDockDoorTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling dock door template $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDockDoorTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dock door template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDockDoorTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRollingDockDoorTemplateCoordinator(store: RollingDockDoorTemplateStore, clock: Clock): RollingDockDoorTemplateCoordinator {
    return RollingDockDoorTemplateCoordinator(
        store = store,
        validator = defaultRollingDockDoorTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
