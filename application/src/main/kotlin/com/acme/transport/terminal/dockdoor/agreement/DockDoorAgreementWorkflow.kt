package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.clock.Clock

/** Moves dock door agreements through their life cycle and keeps a journal of what it did. */
data class DockDoorAgreementService(
    var store: DockDoorAgreementStore,
    var validator: DockDoorAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorAgreementStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorAgreementStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newDockDoorAgreementService(store: DockDoorAgreementStore, clock: Clock): DockDoorAgreementService {
    return DockDoorAgreementService(
        store = store,
        validator = defaultDockDoorAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
