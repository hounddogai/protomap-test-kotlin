package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.clock.Clock

/** Moves global broker summaries through their life cycle and keeps a journal of what it did. */
data class GlobalBrokerSummaryManager(
    var store: GlobalBrokerSummaryStore,
    var validator: GlobalBrokerSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global broker summary $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalBrokerSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global broker summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalBrokerSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newGlobalBrokerSummaryManager(store: GlobalBrokerSummaryStore, clock: Clock): GlobalBrokerSummaryManager {
    return GlobalBrokerSummaryManager(
        store = store,
        validator = defaultGlobalBrokerSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
