package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.Clock

/** Moves tiered trailer yard templates through their life cycle and keeps a journal of what it did. */
data class TieredTrailerYardTemplateDesk(
    var store: TieredTrailerYardTemplateStore,
    var validator: TieredTrailerYardTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered trailer yard template $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTrailerYardTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered trailer yard template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTrailerYardTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newTieredTrailerYardTemplateDesk(store: TieredTrailerYardTemplateStore, clock: Clock): TieredTrailerYardTemplateDesk {
    return TieredTrailerYardTemplateDesk(
        store = store,
        validator = defaultTieredTrailerYardTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
