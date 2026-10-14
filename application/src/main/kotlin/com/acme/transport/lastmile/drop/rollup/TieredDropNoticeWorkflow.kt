package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.clock.Clock

/** Moves tiered drop notices through their life cycle and keeps a journal of what it did. */
data class TieredDropNoticeWorkflow(
    var store: TieredDropNoticeStore,
    var validator: TieredDropNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDropNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered drop notice $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDropNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered drop notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDropNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered drop notice $code not found")
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

fun newTieredDropNoticeWorkflow(store: TieredDropNoticeStore, clock: Clock): TieredDropNoticeWorkflow {
    return TieredDropNoticeWorkflow(
        store = store,
        validator = defaultTieredDropNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDropNoticeEngine(),
    )
}
