package com.acme.transport.terminal.dwell.notice

import com.acme.transport.terminal.clock.Clock

/** Moves lenient dwell templates through their life cycle and keeps a journal of what it did. */
data class LenientDwellTemplateCoordinator(
    var store: LenientDwellTemplateStore,
    var validator: LenientDwellTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDwellTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient dwell template $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDwellTemplateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dwell template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDwellTemplateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient dwell template $code not found")
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

fun newLenientDwellTemplateCoordinator(store: LenientDwellTemplateStore, clock: Clock): LenientDwellTemplateCoordinator {
    return LenientDwellTemplateCoordinator(
        store = store,
        validator = defaultLenientDwellTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDwellTemplateEngine(),
    )
}
