package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal locker segments through their life cycle and keeps a journal of what it did. */
data class SeasonalLockerSegmentCoordinator(
    var store: SeasonalLockerSegmentStore,
    var validator: SeasonalLockerSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalLockerSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal locker segment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalLockerSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal locker segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalLockerSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal locker segment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalLockerSegmentCoordinator(store: SeasonalLockerSegmentStore, clock: Clock): SeasonalLockerSegmentCoordinator {
    return SeasonalLockerSegmentCoordinator(
        store = store,
        validator = defaultSeasonalLockerSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalLockerSegmentEngine(),
    )
}
