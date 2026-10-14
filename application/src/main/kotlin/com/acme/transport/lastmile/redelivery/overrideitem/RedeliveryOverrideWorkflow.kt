package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery overrides through their life cycle and keeps a journal of what it did. */
data class RedeliveryOverrideDesk(
    var store: RedeliveryOverrideStore,
    var validator: RedeliveryOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("redelivery override $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryOverrideStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery override $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRedeliveryOverrideDesk(store: RedeliveryOverrideStore, clock: Clock): RedeliveryOverrideDesk {
    return RedeliveryOverrideDesk(
        store = store,
        validator = defaultRedeliveryOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryOverrideEngine(),
    )
}
