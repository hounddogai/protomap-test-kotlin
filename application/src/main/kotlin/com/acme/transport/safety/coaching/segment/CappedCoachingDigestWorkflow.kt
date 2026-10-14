package com.acme.transport.safety.coaching.segment

import com.acme.transport.safety.clock.Clock

/** Moves capped coaching digests through their life cycle and keeps a journal of what it did. */
data class CappedCoachingDigestService(
    var store: CappedCoachingDigestStore,
    var validator: CappedCoachingDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCoachingDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped coaching digest $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCoachingDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped coaching digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCoachingDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped coaching digest $code not found")
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

fun newCappedCoachingDigestService(store: CappedCoachingDigestStore, clock: Clock): CappedCoachingDigestService {
    return CappedCoachingDigestService(
        store = store,
        validator = defaultCappedCoachingDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCoachingDigestEngine(),
    )
}
