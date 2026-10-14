package com.acme.transport.linehaul.tender.quota

import com.acme.transport.linehaul.clock.Clock

/** Moves global tender quotas through their life cycle and keeps a journal of what it did. */
data class GlobalTenderQuotaManager(
    var store: GlobalTenderQuotaStore,
    var validator: GlobalTenderQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global tender quota $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTenderQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global tender quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTenderQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newGlobalTenderQuotaManager(store: GlobalTenderQuotaStore, clock: Clock): GlobalTenderQuotaManager {
    return GlobalTenderQuotaManager(
        store = store,
        validator = defaultGlobalTenderQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
