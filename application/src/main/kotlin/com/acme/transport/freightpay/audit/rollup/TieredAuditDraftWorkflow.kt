package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves tiered audit drafts through their life cycle and keeps a journal of what it did. */
data class TieredAuditDraftWorkflow(
    var store: TieredAuditDraftStore,
    var validator: TieredAuditDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredAuditDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered audit draft $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAuditDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered audit draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAuditDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered audit draft $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredAuditDraftWorkflow(store: TieredAuditDraftStore, clock: Clock): TieredAuditDraftWorkflow {
    return TieredAuditDraftWorkflow(
        store = store,
        validator = defaultTieredAuditDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredAuditDraftEngine(),
    )
}
