package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.clock.Clock

/** Moves tariff snapshots through their life cycle and keeps a journal of what it did. */
data class TariffSnapshotWorkflow(
    var store: TariffSnapshotStore,
    var validator: TariffSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tariff snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(TariffSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newTariffSnapshotWorkflow(store: TariffSnapshotStore, clock: Clock): TariffSnapshotWorkflow {
    return TariffSnapshotWorkflow(
        store = store,
        validator = defaultTariffSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
