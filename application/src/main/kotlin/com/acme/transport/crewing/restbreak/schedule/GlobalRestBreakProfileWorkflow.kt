package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.clock.Clock

/** Moves global rest break profiles through their life cycle and keeps a journal of what it did. */
data class GlobalRestBreakProfileWorkflow(
    var store: GlobalRestBreakProfileStore,
    var validator: GlobalRestBreakProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global rest break profile $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRestBreakProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global rest break profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRestBreakProfileStatus.ARCHIVED)
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

fun newGlobalRestBreakProfileWorkflow(store: GlobalRestBreakProfileStore, clock: Clock): GlobalRestBreakProfileWorkflow {
    return GlobalRestBreakProfileWorkflow(
        store = store,
        validator = defaultGlobalRestBreakProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
