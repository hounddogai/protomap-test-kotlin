package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.safety.clock.Clock

/** Moves rolling near miss segments through their life cycle and keeps a journal of what it did. */
data class RollingNearMissSegmentDesk(
    var store: RollingNearMissSegmentStore,
    var validator: RollingNearMissSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingNearMissSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling near miss segment $code not found")
        validator.requireValid(item)
        item.moveTo(RollingNearMissSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling near miss segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingNearMissSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling near miss segment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRollingNearMissSegmentDesk(store: RollingNearMissSegmentStore, clock: Clock): RollingNearMissSegmentDesk {
    return RollingNearMissSegmentDesk(
        store = store,
        validator = defaultRollingNearMissSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingNearMissSegmentEngine(),
    )
}
