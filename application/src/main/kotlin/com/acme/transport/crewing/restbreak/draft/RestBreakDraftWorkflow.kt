package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.clock.Clock

/** Moves rest break drafts through their life cycle and keeps a journal of what it did. */
data class RestBreakDraftWorkflow(
    var store: RestBreakDraftStore,
    var validator: RestBreakDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break draft $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rest break draft $code not found")
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

fun newRestBreakDraftWorkflow(store: RestBreakDraftStore, clock: Clock): RestBreakDraftWorkflow {
    return RestBreakDraftWorkflow(
        store = store,
        validator = defaultRestBreakDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakDraftEngine(),
    )
}
