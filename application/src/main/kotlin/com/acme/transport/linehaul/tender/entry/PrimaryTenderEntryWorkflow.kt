package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves primary tender entries through their life cycle and keeps a journal of what it did. */
data class PrimaryTenderEntryManager(
    var store: PrimaryTenderEntryStore,
    var validator: PrimaryTenderEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTenderEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary tender entry $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTenderEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tender entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTenderEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary tender entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryTenderEntryManager(store: PrimaryTenderEntryStore, clock: Clock, flags: Registry): PrimaryTenderEntryManager {
    return PrimaryTenderEntryManager(
        store = store,
        validator = defaultPrimaryTenderEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTenderEntryEngine(flags),
    )
}
