package com.acme.transport.terminal.dwell.group

import com.acme.transport.terminal.clock.Clock

/** Moves manual dwell segments through their life cycle and keeps a journal of what it did. */
data class ManualDwellSegmentManager(
    var store: ManualDwellSegmentStore,
    var validator: ManualDwellSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDwellSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual dwell segment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDwellSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual dwell segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDwellSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual dwell segment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newManualDwellSegmentManager(store: ManualDwellSegmentStore, clock: Clock): ManualDwellSegmentManager {
    return ManualDwellSegmentManager(
        store = store,
        validator = defaultManualDwellSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDwellSegmentEngine(),
    )
}
