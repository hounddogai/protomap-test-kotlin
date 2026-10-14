package com.acme.transport.customs.hold.adjustment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves hold scores through their life cycle and keeps a journal of what it did. */
data class HoldScoreDesk(
    var store: HoldScoreStore,
    var validator: HoldScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold score $code not found")
        validator.requireValid(item)
        item.moveTo(HoldScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("hold score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hold score $code not found")
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

fun newHoldScoreDesk(store: HoldScoreStore, clock: Clock, flags: Registry): HoldScoreDesk {
    return HoldScoreDesk(
        store = store,
        validator = defaultHoldScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldScoreEngine(flags),
    )
}
