package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.lastmile.clock.Clock

/** Moves regional parcel entries through their life cycle and keeps a journal of what it did. */
data class RegionalParcelEntryService(
    var store: RegionalParcelEntryStore,
    var validator: RegionalParcelEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional parcel entry $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalParcelEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional parcel entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalParcelEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRegionalParcelEntryService(store: RegionalParcelEntryStore, clock: Clock): RegionalParcelEntryService {
    return RegionalParcelEntryService(
        store = store,
        validator = defaultRegionalParcelEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
