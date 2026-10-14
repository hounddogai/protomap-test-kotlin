package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.clock.Clock

/** Moves pending inspection bundles through their life cycle and keeps a journal of what it did. */
data class PendingInspectionBundleService(
    var store: PendingInspectionBundleStore,
    var validator: PendingInspectionBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending inspection bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPendingInspectionBundleService(store: PendingInspectionBundleStore, clock: Clock): PendingInspectionBundleService {
    return PendingInspectionBundleService(
        store = store,
        validator = defaultPendingInspectionBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
