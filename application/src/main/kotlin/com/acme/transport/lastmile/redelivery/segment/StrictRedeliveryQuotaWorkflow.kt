package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.lastmile.clock.Clock

/** Moves strict redelivery quotas through their life cycle and keeps a journal of what it did. */
data class StrictRedeliveryQuotaService(
    var store: StrictRedeliveryQuotaStore,
    var validator: StrictRedeliveryQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict redelivery quota $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRedeliveryQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict redelivery quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRedeliveryQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newStrictRedeliveryQuotaService(store: StrictRedeliveryQuotaStore, clock: Clock): StrictRedeliveryQuotaService {
    return StrictRedeliveryQuotaService(
        store = store,
        validator = defaultStrictRedeliveryQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
