package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves cutoff profiles through their life cycle and keeps a journal of what it did. */
data class CutoffProfileDesk(
    var store: CutoffProfileStore,
    var validator: CutoffProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cutoff profile $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("cutoff profile $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newCutoffProfileDesk(store: CutoffProfileStore, clock: Clock, flags: Registry): CutoffProfileDesk {
    return CutoffProfileDesk(
        store = store,
        validator = defaultCutoffProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffProfileEngine(flags),
    )
}
