package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.clock.Clock

/** Moves hold estimates through their life cycle and keeps a journal of what it did. */
data class HoldEstimateCoordinator(
    var store: HoldEstimateStore,
    var validator: HoldEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hold estimate $code not found")
        validator.requireValid(item)
        item.moveTo(HoldEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hold estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("hold estimate $code not found")
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

fun newHoldEstimateCoordinator(store: HoldEstimateStore, clock: Clock): HoldEstimateCoordinator {
    return HoldEstimateCoordinator(
        store = store,
        validator = defaultHoldEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldEstimateEngine(),
    )
}
