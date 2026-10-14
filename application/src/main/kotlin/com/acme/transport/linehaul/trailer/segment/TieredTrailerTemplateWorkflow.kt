package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tiered trailer templates through their life cycle and keeps a journal of what it did. */
data class TieredTrailerTemplateCoordinator(
    var store: TieredTrailerTemplateStore,
    var validator: TieredTrailerTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredTrailerTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered trailer template $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTrailerTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered trailer template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTrailerTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered trailer template $code not found")
        val result = engine.run(item)
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

fun newTieredTrailerTemplateCoordinator(store: TieredTrailerTemplateStore, clock: Clock, flags: Registry): TieredTrailerTemplateCoordinator {
    return TieredTrailerTemplateCoordinator(
        store = store,
        validator = defaultTieredTrailerTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredTrailerTemplateEngine(flags),
    )
}
