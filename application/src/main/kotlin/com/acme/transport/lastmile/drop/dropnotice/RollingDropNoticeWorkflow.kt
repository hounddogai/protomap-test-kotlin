package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves rolling drop notices through their life cycle and keeps a journal of what it did. */
data class RollingDropNoticeDesk(
    var store: RollingDropNoticeStore,
    var validator: RollingDropNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDropNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling drop notice $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDropNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling drop notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDropNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling drop notice $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRollingDropNoticeDesk(store: RollingDropNoticeStore, clock: Clock, flags: Registry): RollingDropNoticeDesk {
    return RollingDropNoticeDesk(
        store = store,
        validator = defaultRollingDropNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDropNoticeEngine(flags),
    )
}
