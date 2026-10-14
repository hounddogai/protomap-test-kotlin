package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.safety.clock.Clock

/** Moves lenient scorecard snapshots through their life cycle and keeps a journal of what it did. */
data class LenientScorecardSnapshotDesk(
    var store: LenientScorecardSnapshotStore,
    var validator: LenientScorecardSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientScorecardSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient scorecard snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientScorecardSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient scorecard snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientScorecardSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient scorecard snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientScorecardSnapshotDesk(store: LenientScorecardSnapshotStore, clock: Clock): LenientScorecardSnapshotDesk {
    return LenientScorecardSnapshotDesk(
        store = store,
        validator = defaultLenientScorecardSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientScorecardSnapshotEngine(),
    )
}
