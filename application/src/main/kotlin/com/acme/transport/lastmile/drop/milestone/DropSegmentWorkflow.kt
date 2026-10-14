package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.lastmile.clock.Clock

/** Moves drop segments through their life cycle and keeps a journal of what it did. */
data class DropSegmentDesk(
    var store: DropSegmentStore,
    var validator: DropSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("drop segment $code not found")
        validator.requireValid(item)
        item.moveTo(DropSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("drop segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("drop segment $code not found")
        val result = engine.evaluate(item)
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

fun newDropSegmentDesk(store: DropSegmentStore, clock: Clock): DropSegmentDesk {
    return DropSegmentDesk(
        store = store,
        validator = defaultDropSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropSegmentEngine(),
    )
}
