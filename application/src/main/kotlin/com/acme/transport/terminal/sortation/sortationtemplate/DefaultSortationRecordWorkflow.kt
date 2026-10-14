package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves default sortation records through their life cycle and keeps a journal of what it did. */
data class DefaultSortationRecordDesk(
    var store: DefaultSortationRecordStore,
    var validator: DefaultSortationRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultSortationRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default sortation record $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultSortationRecordStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default sortation record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultSortationRecordStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default sortation record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newDefaultSortationRecordDesk(store: DefaultSortationRecordStore, clock: Clock, flags: Registry): DefaultSortationRecordDesk {
    return DefaultSortationRecordDesk(
        store = store,
        validator = defaultDefaultSortationRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultSortationRecordEngine(flags),
    )
}
