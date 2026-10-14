package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer records through their life cycle and keeps a journal of what it did. */
data class CappedTrailerRecordDesk(
    var store: CappedTrailerRecordStore,
    var validator: CappedTrailerRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTrailerRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped trailer record $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerRecordStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerRecordStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped trailer record $code not found")
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

fun newCappedTrailerRecordDesk(store: CappedTrailerRecordStore, clock: Clock): CappedTrailerRecordDesk {
    return CappedTrailerRecordDesk(
        store = store,
        validator = defaultCappedTrailerRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTrailerRecordEngine(),
    )
}
