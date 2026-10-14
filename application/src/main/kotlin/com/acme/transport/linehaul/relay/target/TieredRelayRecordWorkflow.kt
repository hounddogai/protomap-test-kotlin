package com.acme.transport.linehaul.relay.target

import com.acme.transport.linehaul.clock.Clock

/** Moves tiered relay records through their life cycle and keeps a journal of what it did. */
data class TieredRelayRecordDesk(
    var store: TieredRelayRecordStore,
    var validator: TieredRelayRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered relay record $code not found")
        validator.requireValid(item)
        item.moveTo(TieredRelayRecordStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered relay record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredRelayRecordStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredRelayRecordDesk(store: TieredRelayRecordStore, clock: Clock): TieredRelayRecordDesk {
    return TieredRelayRecordDesk(
        store = store,
        validator = defaultTieredRelayRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
