package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback lane plan exceptions through their life cycle and keeps a journal of what it did. */
data class FallbackLanePlanExceptionManager(
    var store: FallbackLanePlanExceptionStore,
    var validator: FallbackLanePlanExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback lane plan exception $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackLanePlanExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback lane plan exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackLanePlanExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFallbackLanePlanExceptionManager(store: FallbackLanePlanExceptionStore, clock: Clock): FallbackLanePlanExceptionManager {
    return FallbackLanePlanExceptionManager(
        store = store,
        validator = defaultFallbackLanePlanExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
