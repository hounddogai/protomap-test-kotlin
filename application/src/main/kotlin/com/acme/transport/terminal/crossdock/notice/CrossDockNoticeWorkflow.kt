package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock notices through their life cycle and keeps a journal of what it did. */
data class CrossDockNoticeCoordinator(
    var store: CrossDockNoticeStore,
    var validator: CrossDockNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cross dock notice $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockNoticeStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockNoticeStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("cross dock notice $code not found")
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

fun newCrossDockNoticeCoordinator(store: CrossDockNoticeStore, clock: Clock): CrossDockNoticeCoordinator {
    return CrossDockNoticeCoordinator(
        store = store,
        validator = defaultCrossDockNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockNoticeEngine(),
    )
}
