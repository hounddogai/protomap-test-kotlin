package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.linehaul.clock.Clock

/** Moves capped tender templates through their life cycle and keeps a journal of what it did. */
data class CappedTenderTemplateWorkflow(
    var store: CappedTenderTemplateStore,
    var validator: CappedTenderTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped tender template $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTenderTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tender template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTenderTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCappedTenderTemplateWorkflow(store: CappedTenderTemplateStore, clock: Clock): CappedTenderTemplateWorkflow {
    return CappedTenderTemplateWorkflow(
        store = store,
        validator = defaultCappedTenderTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
