package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.clock.Clock

/** Moves default ev charge segments through their life cycle and keeps a journal of what it did. */
data class DefaultEvChargeSegmentDesk(
    var store: DefaultEvChargeSegmentStore,
    var validator: DefaultEvChargeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default ev charge segment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEvChargeSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default ev charge segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEvChargeSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDefaultEvChargeSegmentDesk(store: DefaultEvChargeSegmentStore, clock: Clock): DefaultEvChargeSegmentDesk {
    return DefaultEvChargeSegmentDesk(
        store = store,
        validator = defaultDefaultEvChargeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
