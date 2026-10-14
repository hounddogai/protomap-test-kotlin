package com.acme.transport.network.transittime.proposal

import com.acme.transport.network.clock.Clock

/** Moves global transit time templates through their life cycle and keeps a journal of what it did. */
data class GlobalTransitTimeTemplateService(
    var store: GlobalTransitTimeTemplateStore,
    var validator: GlobalTransitTimeTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global transit time template $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTransitTimeTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global transit time template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTransitTimeTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newGlobalTransitTimeTemplateService(store: GlobalTransitTimeTemplateStore, clock: Clock): GlobalTransitTimeTemplateService {
    return GlobalTransitTimeTemplateService(
        store = store,
        validator = defaultGlobalTransitTimeTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
