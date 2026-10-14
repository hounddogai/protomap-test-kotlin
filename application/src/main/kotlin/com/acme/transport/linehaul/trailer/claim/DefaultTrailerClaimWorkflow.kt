package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.linehaul.clock.Clock

/** Moves default trailer claims through their life cycle and keeps a journal of what it did. */
data class DefaultTrailerClaimDesk(
    var store: DefaultTrailerClaimStore,
    var validator: DefaultTrailerClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTrailerClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default trailer claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTrailerClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default trailer claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTrailerClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default trailer claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultTrailerClaimDesk(store: DefaultTrailerClaimStore, clock: Clock): DefaultTrailerClaimDesk {
    return DefaultTrailerClaimDesk(
        store = store,
        validator = defaultDefaultTrailerClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTrailerClaimEngine(),
    )
}
