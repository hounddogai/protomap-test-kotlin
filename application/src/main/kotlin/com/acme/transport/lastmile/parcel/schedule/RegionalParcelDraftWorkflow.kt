package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.lastmile.clock.Clock

/** Moves regional parcel drafts through their life cycle and keeps a journal of what it did. */
data class RegionalParcelDraftManager(
    var store: RegionalParcelDraftStore,
    var validator: RegionalParcelDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalParcelDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional parcel draft $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalParcelDraftStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional parcel draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalParcelDraftStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional parcel draft $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRegionalParcelDraftManager(store: RegionalParcelDraftStore, clock: Clock): RegionalParcelDraftManager {
    return RegionalParcelDraftManager(
        store = store,
        validator = defaultRegionalParcelDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalParcelDraftEngine(),
    )
}
