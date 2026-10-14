package com.acme.transport.safety.scorecard.quota

import com.acme.transport.safety.clock.Clock

/** Moves strict scorecard templates through their life cycle and keeps a journal of what it did. */
data class StrictScorecardTemplateManager(
    var store: StrictScorecardTemplateStore,
    var validator: StrictScorecardTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictScorecardTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict scorecard template $code not found")
        validator.requireValid(item)
        item.moveTo(StrictScorecardTemplateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict scorecard template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictScorecardTemplateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict scorecard template $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictScorecardTemplateManager(store: StrictScorecardTemplateStore, clock: Clock): StrictScorecardTemplateManager {
    return StrictScorecardTemplateManager(
        store = store,
        validator = defaultStrictScorecardTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictScorecardTemplateEngine(),
    )
}
