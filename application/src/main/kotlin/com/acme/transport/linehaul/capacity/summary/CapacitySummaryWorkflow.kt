package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity summaries through their life cycle and keeps a journal of what it did. */
data class CapacitySummaryService(
    var store: CapacitySummaryStore,
    var validator: CapacitySummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacitySummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capacity summary $code not found")
        validator.requireValid(item)
        item.moveTo(CapacitySummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacitySummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capacity summary $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newCapacitySummaryService(store: CapacitySummaryStore, clock: Clock): CapacitySummaryService {
    return CapacitySummaryService(
        store = store,
        validator = defaultCapacitySummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacitySummaryEngine(),
    )
}
