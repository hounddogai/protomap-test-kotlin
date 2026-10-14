package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled dispute bundles through their life cycle and keeps a journal of what it did. */
data class ScheduledDisputeBundleDesk(
    var store: ScheduledDisputeBundleStore,
    var validator: ScheduledDisputeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled dispute bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDisputeBundleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dispute bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDisputeBundleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newScheduledDisputeBundleDesk(store: ScheduledDisputeBundleStore, clock: Clock): ScheduledDisputeBundleDesk {
    return ScheduledDisputeBundleDesk(
        store = store,
        validator = defaultScheduledDisputeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
