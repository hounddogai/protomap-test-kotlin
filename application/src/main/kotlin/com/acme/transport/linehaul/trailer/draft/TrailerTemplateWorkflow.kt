package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer templates through their life cycle and keeps a journal of what it did. */
data class TrailerTemplateCoordinator(
    var store: TrailerTemplateStore,
    var validator: TrailerTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer template $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("trailer template $code not found")
        val result = engine.evaluate(item)
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

fun newTrailerTemplateCoordinator(store: TrailerTemplateStore, clock: Clock, flags: Registry): TrailerTemplateCoordinator {
    return TrailerTemplateCoordinator(
        store = store,
        validator = defaultTrailerTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerTemplateEngine(flags),
    )
}
