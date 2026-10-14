package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback lane plan summaries through their life cycle and keeps a journal of what it did. */
data class FallbackLanePlanSummaryService(
    var store: FallbackLanePlanSummaryStore,
    var validator: FallbackLanePlanSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackLanePlanSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback lane plan summary $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackLanePlanSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback lane plan summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackLanePlanSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback lane plan summary $code not found")
        val result = engine.compute(item)
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

fun newFallbackLanePlanSummaryService(store: FallbackLanePlanSummaryStore, clock: Clock): FallbackLanePlanSummaryService {
    return FallbackLanePlanSummaryService(
        store = store,
        validator = defaultFallbackLanePlanSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackLanePlanSummaryEngine(),
    )
}
