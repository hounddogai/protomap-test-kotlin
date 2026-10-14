package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves default attempt notices through their life cycle and keeps a journal of what it did. */
data class DefaultAttemptNoticeWorkflow(
    var store: DefaultAttemptNoticeStore,
    var validator: DefaultAttemptNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default attempt notice $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAttemptNoticeStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default attempt notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAttemptNoticeStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDefaultAttemptNoticeWorkflow(store: DefaultAttemptNoticeStore, clock: Clock): DefaultAttemptNoticeWorkflow {
    return DefaultAttemptNoticeWorkflow(
        store = store,
        validator = defaultDefaultAttemptNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
