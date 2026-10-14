package com.acme.transport.lastmile.locker.score

import com.acme.transport.lastmile.clock.Clock

/** Moves manual locker quotas through their life cycle and keeps a journal of what it did. */
data class ManualLockerQuotaManager(
    var store: ManualLockerQuotaStore,
    var validator: ManualLockerQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual locker quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualLockerQuotaStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual locker quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualLockerQuotaStatus.RETIRED)
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

fun newManualLockerQuotaManager(store: ManualLockerQuotaStore, clock: Clock): ManualLockerQuotaManager {
    return ManualLockerQuotaManager(
        store = store,
        validator = defaultManualLockerQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
