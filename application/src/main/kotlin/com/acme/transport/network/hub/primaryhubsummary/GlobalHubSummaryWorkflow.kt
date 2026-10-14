package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global hub summaries through their life cycle and keeps a journal of what it did. */
data class GlobalHubSummaryDesk(
    var store: GlobalHubSummaryStore,
    var validator: GlobalHubSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalHubSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global hub summary $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalHubSummaryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global hub summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalHubSummaryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global hub summary $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newGlobalHubSummaryDesk(store: GlobalHubSummaryStore, clock: Clock, flags: Registry): GlobalHubSummaryDesk {
    return GlobalHubSummaryDesk(
        store = store,
        validator = defaultGlobalHubSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalHubSummaryEngine(flags),
    )
}
