package com.acme.transport.network.transittime.slot

import com.acme.transport.network.clock.Clock

/** Moves transit time templates through their life cycle and keeps a journal of what it did. */
data class TransitTimeTemplateDesk(
    var store: TransitTimeTemplateStore,
    var validator: TransitTimeTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("transit time template $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("transit time template $code not found")
        val result = engine.compute(item)
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

fun newTransitTimeTemplateDesk(store: TransitTimeTemplateStore, clock: Clock): TransitTimeTemplateDesk {
    return TransitTimeTemplateDesk(
        store = store,
        validator = defaultTransitTimeTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeTemplateEngine(),
    )
}
