package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.lastmile.clock.Clock

/** Moves strict attempt templates through their life cycle and keeps a journal of what it did. */
data class StrictAttemptTemplateCoordinator(
    var store: StrictAttemptTemplateStore,
    var validator: StrictAttemptTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict attempt template $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAttemptTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict attempt template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAttemptTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newStrictAttemptTemplateCoordinator(store: StrictAttemptTemplateStore, clock: Clock): StrictAttemptTemplateCoordinator {
    return StrictAttemptTemplateCoordinator(
        store = store,
        validator = defaultStrictAttemptTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
