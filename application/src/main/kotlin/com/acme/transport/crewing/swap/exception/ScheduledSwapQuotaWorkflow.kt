package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled swap quotas through their life cycle and keeps a journal of what it did. */
data class ScheduledSwapQuotaDesk(
    var store: ScheduledSwapQuotaStore,
    var validator: ScheduledSwapQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledSwapQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled swap quota $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSwapQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled swap quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSwapQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled swap quota $code not found")
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

fun newScheduledSwapQuotaDesk(store: ScheduledSwapQuotaStore, clock: Clock): ScheduledSwapQuotaDesk {
    return ScheduledSwapQuotaDesk(
        store = store,
        validator = defaultScheduledSwapQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledSwapQuotaEngine(),
    )
}
