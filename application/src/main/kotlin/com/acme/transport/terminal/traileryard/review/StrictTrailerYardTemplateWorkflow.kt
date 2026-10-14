package com.acme.transport.terminal.traileryard.review

import com.acme.transport.terminal.clock.Clock

/** Moves strict trailer yard templates through their life cycle and keeps a journal of what it did. */
data class StrictTrailerYardTemplateService(
    var store: StrictTrailerYardTemplateStore,
    var validator: StrictTrailerYardTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict trailer yard template $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTrailerYardTemplateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict trailer yard template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTrailerYardTemplateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictTrailerYardTemplateService(store: StrictTrailerYardTemplateStore, clock: Clock): StrictTrailerYardTemplateService {
    return StrictTrailerYardTemplateService(
        store = store,
        validator = defaultStrictTrailerYardTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
