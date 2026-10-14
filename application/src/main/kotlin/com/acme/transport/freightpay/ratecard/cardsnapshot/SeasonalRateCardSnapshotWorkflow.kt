package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves seasonal rate card snapshots through their life cycle and keeps a journal of what it did. */
data class SeasonalRateCardSnapshotManager(
    var store: SeasonalRateCardSnapshotStore,
    var validator: SeasonalRateCardSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRateCardSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal rate card snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRateCardSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal rate card snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRateCardSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal rate card snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalRateCardSnapshotManager(store: SeasonalRateCardSnapshotStore, clock: Clock): SeasonalRateCardSnapshotManager {
    return SeasonalRateCardSnapshotManager(
        store = store,
        validator = defaultSeasonalRateCardSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRateCardSnapshotEngine(),
    )
}
