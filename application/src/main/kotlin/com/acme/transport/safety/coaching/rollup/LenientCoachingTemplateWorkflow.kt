package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves lenient coaching templates through their life cycle and keeps a journal of what it did. */
data class LenientCoachingTemplateManager(
    var store: LenientCoachingTemplateStore,
    var validator: LenientCoachingTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCoachingTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient coaching template $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCoachingTemplateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient coaching template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCoachingTemplateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient coaching template $code not found")
        val result = engine.evaluate(item)
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

fun newLenientCoachingTemplateManager(store: LenientCoachingTemplateStore, clock: Clock, flags: Registry): LenientCoachingTemplateManager {
    return LenientCoachingTemplateManager(
        store = store,
        validator = defaultLenientCoachingTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCoachingTemplateEngine(flags),
    )
}
